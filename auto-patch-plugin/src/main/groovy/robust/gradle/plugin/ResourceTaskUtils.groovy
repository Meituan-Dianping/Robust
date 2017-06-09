package robust.gradle.plugin

import com.meituan.robust.Constants
import javassist.*
import javassist.bytecode.AccessFlag

import java.util.jar.JarOutputStream
import java.util.zip.ZipOutputStream
/**
 * Created by hedex on 16/11/3.
 */
class ResourceTaskUtils {
    private ResourceTaskUtils(){

    }
    public static void keepCode(List<CtClass> box, File jarFile) throws CannotCompileException, IOException, NotFoundException {
        ZipOutputStream outStream=new JarOutputStream(new FileOutputStream(jarFile));
//        new ForkJoinPool().submit {
        for(CtClass ctClass:box) {
            if (isNeedInsertClass(ctClass.getName())) {
                ctClass.setModifiers(AccessFlag.setPublic(ctClass.getModifiers()));
                if (ctClass.isInterface() || ctClass.getDeclaredMethods().length < 1) {
                    zipFile(ctClass.toBytecode(), outStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
                    continue;
                }

                boolean addIncrementalChange = false;
                for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
                    if (!addIncrementalChange) {
                        addIncrementalChange = true;
                        ClassPool classPool = ctBehavior.getDeclaringClass().getClassPool();
                        CtClass type = classPool.getOrNull(Constants.INTERFACE_NAME);
                        CtField ctField = new CtField(type, Constants.INSERT_FIELD_NAME, ctClass);
                        ctField.setModifiers(AccessFlag.PUBLIC | AccessFlag.STATIC);
                        ctClass.addField(ctField);
                    }
                    if(!isQualifiedMethod(ctBehavior)){
                        continue;
                    }
                    //here comes the method will be inserted code
                    methodMap.put(ctBehavior.getLongName(), insertMethodCount.incrementAndGet());
                }
            }
            zipFile(ctClass.toBytecode(), outStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
        }
        outStream.close();
    }


}