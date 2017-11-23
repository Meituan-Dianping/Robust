package com.meituan.robust.autopatch

import com.meituan.robust.Constants
import com.meituan.robust.patch.annotaion.Add
import com.meituan.robust.patch.annotaion.Modify
import com.meituan.robust.utils.JavaUtils
import javassist.CannotCompileException
import javassist.CtClass
import javassist.CtMethod
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import org.codehaus.groovy.GroovyException
import org.gradle.api.logging.Logger
import robust.gradle.plugin.AutoPatchTransform

class ReadAnnotation {
    static Logger logger

    public static void readAnnotation(List<CtClass> box, Logger log) {
        logger = log;
        Set patchMethodSignureSet = new HashSet<String>();
        synchronized (AutoPatchTransform.class) {
            if (Constants.ModifyAnnotationClass == null) {
                Constants.ModifyAnnotationClass = box.get(0).getClassPool().get(Constants.MODIFY_ANNOTATION).toClass();
            }
            if (Constants.AddAnnotationClass == null) {
                Constants.AddAnnotationClass = box.get(0).getClassPool().get(Constants.ADD_ANNOTATION).toClass();
            }
        }
        box.forEach {
            ctclass ->
                try {
                    boolean isNewlyAddClass = scanClassForAddClassAnnotation(ctclass);
                    //newly add class donnot need scann for modify
                    if (!isNewlyAddClass) {
                        patchMethodSignureSet.addAll(scanClassForModifyMethod(ctclass));
                        scanClassForAddMethodAnnotation(ctclass);
                    }
                } catch (NullPointerException e) {
                    logger.warn("something wrong when readAnnotation, " + e.getMessage() + " cannot find class name " + ctclass.name)
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    logger.warn("something wrong when readAnnotation, " + e.getMessage() + " cannot find class name " + ctclass.name)
                    e.printStackTrace();
                }
        }
        println("new add methods  list is ")
        JavaUtils.printList(Config.newlyAddedMethodSet.toList())
        println("new add classes list is ")
        JavaUtils.printList(Config.newlyAddedClassNameList)
        println(" patchMethodSignatureSet is printed below ")
        JavaUtils.printList(patchMethodSignureSet.asList())
        Config.patchMethodSignatureSet.addAll(patchMethodSignureSet);
    }

    public static boolean scanClassForAddClassAnnotation(CtClass ctclass) {

        Add addClassAnootation = ctclass.getAnnotation(Constants.AddAnnotationClass) as Add;
        if (addClassAnootation != null && !Config.newlyAddedClassNameList.contains(ctclass.name)) {
            Config.newlyAddedClassNameList.add(ctclass.name);
            return true;
        }

        return false;
    }

    public static void scanClassForAddMethodAnnotation(CtClass ctclass) {

        ctclass.defrost();
        ctclass.declaredMethods.each { method ->
            if (null != method.getAnnotation(Constants.AddAnnotationClass)) {
                Config.newlyAddedMethodSet.add(method.longName)
            }
        }
    }

    public static Set scanClassForModifyMethod(CtClass ctclass) {
        Set patchMethodSignureSet = new HashSet<String>();
        boolean isAllMethodsPatch = true;
        ctclass.declaredMethods.findAll {
            return it.hasAnnotation(Constants.ModifyAnnotationClass);
        }.each {
            method ->
                isAllMethodsPatch = false;
                addPatchMethodAndModifiedClass(patchMethodSignureSet, method);
        }

        //do with lamda expression
        ctclass.defrost();
        ctclass.declaredMethods.findAll {
            return Config.methodMap.get(it.longName) != null;
        }.each { method ->
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    try {

                        if (Constants.LAMBDA_MODIFY.equals(m.method.declaringClass.name)) {
                            isAllMethodsPatch = false;
                            addPatchMethodAndModifiedClass(patchMethodSignureSet, method);
                        }
                    } catch (javassist.NotFoundException e) {
                        e.printStackTrace()
                        logger.warn("  cannot find class  " + method.longName + " line number " + m.lineNumber + " this class may never used ,please remove this class");
                    }
                }
            });
        }
        Modify classModifyAnootation = ctclass.getAnnotation(Constants.ModifyAnnotationClass) as Modify;
        if (classModifyAnootation != null) {
            if (isAllMethodsPatch) {
                if (classModifyAnootation.value().length() < 1) {
                    ctclass.declaredMethods.findAll {
                        return Config.methodMap.get(it.longName) != null;
                    }.each { method ->
                        addPatchMethodAndModifiedClass(patchMethodSignureSet, method);
                    }
                } else {
                    ctclass.getClassPool().get(classModifyAnootation.value()).declaredMethods.findAll {
                        return Config.methodMap.get(it.longName) != null;
                    }.each { method ->
                        addPatchMethodAndModifiedClass(patchMethodSignureSet, method);
                    }
                }
            }
        }
        return patchMethodSignureSet;
    }

    public static Set addPatchMethodAndModifiedClass(Set patchMethodSignureSet, CtMethod method) {
        if (Config.methodMap.get(method.longName) == null) {
            print("addPatchMethodAndModifiedClass pint methodmap ");
            JavaUtils.printMap(Config.methodMap);
            throw new GroovyException("patch method " + method.longName + " haven't insert code by Robust.Cannot patch this method, method.signature  " + method.signature + "  ");
        }
        Modify methodModifyAnootation = method.getAnnotation(Constants.ModifyAnnotationClass) as Modify;
        Modify classModifyAnootation = method.declaringClass.getAnnotation(Constants.ModifyAnnotationClass) as Modify;
        if ((methodModifyAnootation == null || methodModifyAnootation.value().length() < 1)) {
            //no annotation value
            patchMethodSignureSet.add(method.longName);
            if (!Config.modifiedClassNameList.contains(method.declaringClass.name))
                Config.modifiedClassNameList.add(method.declaringClass.name);
        } else {
            //use value in annotation
            patchMethodSignureSet.add(methodModifyAnootation.value());
        }
        if (classModifyAnootation == null || classModifyAnootation.value().length() < 1) {
            if (!Config.modifiedClassNameList.contains(method.declaringClass.name)) {
                Config.modifiedClassNameList.add(method.declaringClass.name);
            }
        } else {
            if (!Config.modifiedClassNameList.contains(classModifyAnootation.value())) {
                Config.modifiedClassNameList.add(classModifyAnootation.value());
            }
        }
        return patchMethodSignureSet;
    }
}
