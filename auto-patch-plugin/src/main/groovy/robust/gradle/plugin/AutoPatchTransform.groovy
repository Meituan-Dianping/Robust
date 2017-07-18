package robust.gradle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.meituan.robust.Constants
import com.meituan.robust.autopatch.*
import com.meituan.robust.utils.JavaUtils
import com.meituan.robust.utils.SmaliTool
import javassist.CannotCompileException
import javassist.CtClass
import javassist.CtMethod
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
/**
 * Created by mivanzhang on 16/7/21.
 *
 * AutoPatchTransform generate patch dex
 */
class AutoPatchTransform extends Transform implements Plugin<Project> {
    private
    static String dex2SmaliCommand;
    private
    static String smali2DexCommand;
    private
    static String jar2DexCommand;
    public static String ROBUST_DIR;
    Project project
    static Logger logger

    @Override
    void apply(Project target) {
        this.project = target
        logger = project.logger
        initConfig();
        project.android.registerTransform(this)
    }

    def initConfig() {
        //clear
        NameManger.init();
        InlineClassFactory.init();
        ReadMapping.init();
        Config.init();

        ROBUST_DIR = "${project.projectDir}${File.separator}robust${File.separator}"
        def baksmaliFilePath = "${ROBUST_DIR}${Constants.LIB_NAME_ARRAY[0]}"
        def smaliFilePath = "${ROBUST_DIR}${Constants.LIB_NAME_ARRAY[1]}"
        def dxFilePath = "${ROBUST_DIR}${Constants.LIB_NAME_ARRAY[2]}"
        Config.robustGenerateDirectory = "${project.buildDir}" + File.separator + "$Constants.ROBUST_GENERATE_DIRECTORY" + File.separator;
        dex2SmaliCommand = "  java -jar ${baksmaliFilePath} -o classout" + File.separator + "  $Constants.CLASSES_DEX_NAME";
        smali2DexCommand = "   java -jar ${smaliFilePath} classout" + File.separator + " -o "+Constants.PATACH_DEX_NAME;
        jar2DexCommand = "   java -jar ${dxFilePath} --dex --output=$Constants.CLASSES_DEX_NAME  " + Constants.ZIP_FILE_NAME;
        ReadXML.readXMl(project.projectDir.path);
        Config.methodMap = JavaUtils.getMapFromZippedFile(project.projectDir.path + Constants.METHOD_MAP_PATH)
    }

    @Override
    String getName() {
        return "AutoPatchTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        def startTime = System.currentTimeMillis()
        logger.quiet '================autoPatch start================'
        copyJarToRobust()
        outputProvider.deleteAll()
        def outDir = outputProvider.getContentLocation("main", outputTypes, scopes, Format.DIRECTORY)
        project.android.bootClasspath.each {
            Config.classPool.appendClassPath((String) it.absolutePath)
        }
        def box = ReflectUtils.toCtClasses(inputs, Config.classPool)
        def cost = (System.currentTimeMillis() - startTime) / 1000
        logger.quiet "check all class cost $cost second, class count: ${box.size()}"
        autoPatch(box)
//        JavaUtils.removeJarFromLibs()
        logger.quiet '================method singure to methodid is printed below================'
        JavaUtils.printMap(Config.methodMap)
        cost = (System.currentTimeMillis() - startTime) / 1000
        logger.quiet "autoPatch cost $cost second"
        throw new RuntimeException("auto patch end successfully")
    }

    static def copyJarToRobust() {
        File targetDir = new File(ROBUST_DIR);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        for (String libName : Constants.LIB_NAME_ARRAY) {
            InputStream inputStream = JavaUtils.class.getResourceAsStream("/libs/" + libName);
            if (inputStream == null) {
                System.out.println("Warning!!!  Did not find " + libName + " ，you must add it to your project's libs ");
                continue;
            }
            File inputFile = new File(ROBUST_DIR + libName);
            try {
                OutputStream inputFileOut = new FileOutputStream(inputFile);
                JavaUtils.copy(inputStream, inputFileOut);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Warning!!! " + libName + " copy error " + e.getMessage());

            }
        }
    }

    def autoPatch(List<CtClass> box) {
        File buildDir = project.getBuildDir();
        String patchPath = buildDir.getAbsolutePath() + File.separator + Constants.ROBUST_GENERATE_DIRECTORY + File.separator;
        clearPatchPath(patchPath);
        ReadAnnotation.readAnnotation(box, logger);
        if(Config.supportProGuard) {
            ReadMapping.getInstance().initMappingInfo();
        }

        generatPatch(box,patchPath);

        zipPatchClassesFile()
        executeCommand(jar2DexCommand)
        executeCommand(dex2SmaliCommand)
        SmaliTool.getInstance().dealObscureInSmali();
        executeCommand(smali2DexCommand)
        //package patch.dex to patch.jar
        packagePatchDex2Jar()
        deleteTmpFiles()
    }
    def  zipPatchClassesFile(){
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(Config.robustGenerateDirectory+ Constants.ZIP_FILE_NAME));
        zipAllPatchClasses(Config.robustGenerateDirectory+Config.patchPackageName.substring(0,Config.patchPackageName.indexOf(".")),"",zipOut);
        zipOut.close();

    }

    def zipAllPatchClasses(String path, String fullClassName, ZipOutputStream zipOut) {
        File file = new File(path);
        if (file.exists()) {
            fullClassName=fullClassName+file.name;
            if (file.isDirectory()) {
                fullClassName+=File.separator;
                File[] files = file.listFiles();
                if (files.length == 0) {
                    return;
                } else {
                    for (File file2 : files) {
                        zipAllPatchClasses(file2.getAbsolutePath(),fullClassName,zipOut);
                    }
                }
            } else {
                //文件
                zipFile(file,zipOut, fullClassName);
            }
        } else {
            logger.debug("文件不存在!");
        }
    }

    def  generatPatch(List<CtClass> box,String patchPath){
        if (!Config.isManual) {
            if (Config.patchMethodSignatureSet.size() < 1) {
                throw new RuntimeException(" patch method is empty ,please check your Modify annotation or use RobustModify.modify() to mark modified methods")
            }
            Config.methodNeedPatchSet.addAll(Config.patchMethodSignatureSet)
            InlineClassFactory.dealInLineClass(patchPath, Config.newlyAddedClassNameList)
            initSuperMethodInClass(Config.modifiedClassNameList);
            //auto generate all class
            for (String fullClassName : Config.modifiedClassNameList) {
                CtClass ctClass = Config.classPool.get(fullClassName)
                CtClass patchClass = PatchesFactory.createPatch(patchPath, ctClass, false, NameManger.getInstance().getPatchName(ctClass.name), Config.patchMethodSignatureSet)
                patchClass.writeFile(patchPath)
                patchClass.defrost();
                createControlClass(patchPath, ctClass)
            }
            createPatchesInfoClass(patchPath);
            if (Config.methodNeedPatchSet.size() > 0) {
                throw new RuntimeException(" some methods haven't patched,see unpatched method list : " + Config.methodNeedPatchSet.toListString())
            }
        } else {
            autoPatchManually(box, patchPath);
        }

    }
    def deleteTmpFiles(){
        File diretcory=new File(Config.robustGenerateDirectory);
        if(!diretcory.isDirectory()){
            throw new RuntimeException("patch directry "+Config.robustGenerateDirectory+" dones not exist");
        }else{
            diretcory.listFiles(new FilenameFilter() {
                @Override
                boolean accept(File file, String s) {
                    return !(Constants.PATACH_JAR_NAME.equals(s))
                }
            }).each {
                if(it.isDirectory()){
                    it.deleteDir()
                }else {
                    it.delete()
                }
            }
        }
    }

    def autoPatchManually(List<CtClass> box, String patchPath) {
        box.forEach { ctClass ->
            if (Config.isManual && ctClass.name.startsWith(Config.patchPackageName)) {
                Config.modifiedClassNameList.add(ctClass.name);
                ctClass.writeFile(patchPath);
            }
        }
    }


    def executeCommand(String commond) {
        Process output = commond.execute(null, new File(Config.robustGenerateDirectory))
        output.inputStream.eachLine { println commond + " inputStream output   " + it }
        output.errorStream.eachLine {
            println commond + " errorStream output   " + it;
            throw new RuntimeException("execute command " + commond + " error");
        }
    }


    def initSuperMethodInClass(List originClassList) {
        CtClass modifiedCtClass;
        for (String modifiedFullClassName : originClassList) {
            List<CtMethod> invokeSuperMethodList = Config.invokeSuperMethodMap.getOrDefault(modifiedFullClassName, new ArrayList());
            //检查当前修改类中使用到类，并加入mapping信息
            modifiedCtClass = Config.classPool.get(modifiedFullClassName);
            modifiedCtClass.defrost();
            modifiedCtClass.declaredMethods.findAll {
                return Config.patchMethodSignatureSet.contains(it.longName)||InlineClassFactory.allInLineMethodLongname.contains(it.longName);
            }.each { behavior ->
                behavior.instrument(new ExprEditor() {
                    @Override
                    void edit(MethodCall m) throws CannotCompileException {
                        if (m.isSuper()) {
                            if (!invokeSuperMethodList.contains(m.method)) {
                                invokeSuperMethodList.add(m.method);
                            }
                        }
                    }
                });
            }
            Config.invokeSuperMethodMap.put(modifiedFullClassName, invokeSuperMethodList);
        }
    }


    def createControlClass(String patchPath, CtClass modifiedClass) {
        CtClass controlClass = PatchesControlFactory.createPatchesControl(modifiedClass);
        controlClass.writeFile(patchPath);
        return controlClass;
    }


    def createPatchesInfoClass(String patchPath) {
        PatchesInfoFactory.createPatchesInfo().writeFile(patchPath);
    }

    def  clearPatchPath(String patchPath) {
        new File(patchPath).deleteDir();
    }

    def  packagePatchDex2Jar() throws IOException {
        File inputFile=new File(Config.robustGenerateDirectory, Constants.PATACH_DEX_NAME);
        if (!inputFile.exists() || !inputFile.canRead()) {
            throw new RuntimeException("patch.dex is not exists or readable")
        }
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(Config.robustGenerateDirectory, Constants.PATACH_JAR_NAME)))
        zipOut.setLevel(Deflater.NO_COMPRESSION)
        FileInputStream fis = new FileInputStream(inputFile)
        zipFile(inputFile,zipOut,Constants.CLASSES_DEX_NAME);
        zipOut.close()
    }

    def zipFile(File inputFile, ZipOutputStream zos, String entryName){
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        FileInputStream fis = new FileInputStream(inputFile)
        byte[] buffer = new byte[4092];
        int byteCount = 0;
        while ((byteCount = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, byteCount);
        }
        fis.close();
        zos.closeEntry();
        zos.flush();
    }


}