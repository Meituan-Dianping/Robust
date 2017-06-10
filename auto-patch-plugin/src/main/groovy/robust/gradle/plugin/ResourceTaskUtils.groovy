package robust.gradle.plugin

import javassist.CannotCompileException
import javassist.CtClass
import javassist.NotFoundException

import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
/**
 * Created by hedex on 16/11/3.
 */
class ResourceTaskUtils {
    private ResourceTaskUtils(){

    }
    public static void keepCode(List<CtClass> box, File jarFile) throws CannotCompileException, IOException, NotFoundException {
        ZipOutputStream outStream=new JarOutputStream(new FileOutputStream(jarFile));
        for(CtClass ctClass:box) {
            zipFile(ctClass.toBytecode(), outStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
        }
        outStream.close();
    }

    private static void zipFile(byte[] classBytesArray, ZipOutputStream zos, String entryName){
        try {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(classBytesArray, 0, classBytesArray.length);
            zos.closeEntry();
            zos.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}