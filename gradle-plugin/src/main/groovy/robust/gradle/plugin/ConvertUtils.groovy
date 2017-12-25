package robust.gradle.plugin

import com.android.SdkConstants
import com.android.build.api.transform.TransformInput
import javassist.ClassPool
import javassist.CtClass

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
/**
 * Created by mivanzhang on 16/11/3.
 */
class ConvertUtils {
    static List<CtClass> toCtClasses(Collection<TransformInput> inputs, ClassPool classPool) {
        List<String> classNames = new ArrayList<>()
        List<CtClass> allClass = new ArrayList<>();
        def startTime = System.currentTimeMillis()
        inputs.each {
            it.directoryInputs.each {
                def dirPath = it.file.absolutePath
                classPool.insertClassPath(it.file.absolutePath)
                org.apache.commons.io.FileUtils.listFiles(it.file, null, true).each {
                    if (it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                        def className = it.absolutePath.substring(dirPath.length() + 1, it.absolutePath.length() - SdkConstants.DOT_CLASS.length()).replaceAll(Matcher.quoteReplacement(File.separator), '.')
                        if(classNames.contains(className)){
                            throw new RuntimeException("You have duplicate classes with the same name : "+className+" please remove duplicate classes ")
                        }
                        classNames.add(className)
                    }
                }
            }

            it.jarInputs.each {
                classPool.insertClassPath(it.file.absolutePath)
                def jarFile = new JarFile(it.file)
                Enumeration<JarEntry> classes = jarFile.entries();
                while (classes.hasMoreElements()) {
                    JarEntry libClass = classes.nextElement();
                    String className = libClass.getName();
                    if (className.endsWith(SdkConstants.DOT_CLASS)) {
                        className = className.substring(0, className.length() - SdkConstants.DOT_CLASS.length()).replaceAll('/', '.')
                        if(classNames.contains(className)){
                            throw new RuntimeException("You have duplicate classes with the same name : "+className+" please remove duplicate classes ")
                        }
                        classNames.add(className)
                    }
                }
            }
        }
        def cost = (System.currentTimeMillis() - startTime) / 1000
        println "read all class file cost $cost second"
        classNames.each {
            try {
                allClass.add(classPool.get(it));
            } catch (javassist.NotFoundException e) {
                println "class not found exception class name:  $it "

            }

        }

        Collections.sort(allClass, new Comparator<CtClass>() {
            @Override
            int compare(CtClass class1, CtClass class2) {
                return class1.getName() <=> class2.getName();
            }
        });
        return allClass;
    }


}