package robust.gradle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.meituan.robust.Constants
import javassist.ClassPool
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import robust.gradle.plugin.asm.AsmInsertImpl
import robust.gradle.plugin.config.GlobalConfig
import robust.gradle.plugin.config.RobustExtension
import robust.gradle.plugin.javaassist.JavaAssistInsertImpl
import robust.gradle.plugin.utils.RobustLog

import java.util.zip.GZIPOutputStream

/**
 * Created by mivanzhang on 16/11/3.
 *
 * insert code
 *
 */
class RobustTransform extends Transform {
    private static final String TAG = "Robust.RobustTransform";
    Project project
    private List<String> hotfixPackageList = new ArrayList<>();
    private List<String> hotfixMethodList = new ArrayList<>();
    private List<String> exceptPackageList = new ArrayList<>();
    private List<String> exceptMethodList = new ArrayList<>();
    private boolean isHotfixMethodLevel = false;
    private boolean isExceptMethodLevel = false;

    InsertcodeStrategy insertcodeStrategy;

    RobustTransform(Project project, List<String> hotfixPackageList, List<String> hotfixMethodList, List<String> exceptPackageList,
                    List<String> exceptMethodList, boolean isHotfixMethodLevel, boolean isExceptMethodLevel) {
        this.project = project
        this.hotfixPackageList = hotfixPackageList
        this.hotfixMethodList = hotfixMethodList
        this.exceptPackageList = exceptPackageList
        this.exceptMethodList = exceptMethodList
        this.isHotfixMethodLevel = isHotfixMethodLevel
        this.isExceptMethodLevel = isExceptMethodLevel
    }

    @Override
    String getName() {
        return "robust"
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
        RobustLog.i(TAG, '================robust start================')
        def startTime = System.currentTimeMillis()
        outputProvider.deleteAll()
        File jarFile = outputProvider.getContentLocation("main", getOutputTypes(), getScopes(),
                Format.JAR);
        if (!jarFile.getParentFile().exists()) {
            jarFile.getParentFile().mkdirs();
        }
        if (jarFile.exists()) {
            jarFile.delete();
        }

        ClassPool classPool = new ClassPool()
        project.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }

        def box = ConvertUtils.toCtClasses(inputs, classPool)
//        RobustLog.i( "check all class cost $cost second, class count: ${box.size()}")
        if (GlobalConfig.turnOnDebug) {
            insertcodeStrategy = new AsmInsertImpl(hotfixPackageList, hotfixMethodList, exceptPackageList, exceptMethodList, isHotfixMethodLevel, isExceptMethodLevel);
        } else {
            insertcodeStrategy = new JavaAssistInsertImpl(hotfixPackageList, hotfixMethodList, exceptPackageList, exceptMethodList, isHotfixMethodLevel, isExceptMethodLevel);
        }
        insertcodeStrategy.insertCode(box, jarFile);
        writeMap2File(insertcodeStrategy.methodMap, Constants.METHOD_MAP_OUT_PATH)

        RobustLog.i(TAG, "===robust print id start===");
        for (String method : insertcodeStrategy.methodMap.keySet()) {
            int id = insertcodeStrategy.methodMap.get(method);
            RobustLog.i(TAG, "key is   " + method + "  value is    " + id);
        }
        RobustLog.i(TAG, "===robust print id end===")

        def cost = (System.currentTimeMillis() - startTime) / 1000
        RobustLog.i(TAG, "robust cost $cost second")
        RobustLog.i(TAG, '================robust   end================')
    }

    private void writeMap2File(Map map, String path) {
        File file = new File(project.buildDir.path + path);
        if (!file.exists() && (!file.parentFile.mkdirs() || !file.createNewFile())) {
//            logger.error(path + " file create error!!")
        }
        FileOutputStream fileOut = new FileOutputStream(file);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(map)
        //gzip压缩
        GZIPOutputStream gzip = new GZIPOutputStream(fileOut);
        gzip.write(byteOut.toByteArray())
        objOut.close();
        gzip.flush();
        gzip.close();
        fileOut.flush()
        fileOut.close()
    }

}