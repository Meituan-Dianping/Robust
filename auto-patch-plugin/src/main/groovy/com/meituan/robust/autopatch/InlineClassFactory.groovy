package com.meituan.robust.autopatch

import com.meituan.robust.utils.JavaUtils
import javassist.CannotCompileException
import javassist.CtClass
import javassist.CtMethod
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

class InlineClassFactory {
    private HashMap<String, List<String>> classInLineMethodsMap = new HashMap<>();
    private static InlineClassFactory inlineClassFactory = new InlineClassFactory();

    private InlineClassFactory() {

    }

    public static void init() {
        inlineClassFactory = new InlineClassFactory();;
    }
    public static Set getAllInLineMethodLongname() {
        Set<String> set=new HashSet<>();
        for(String key:inlineClassFactory.classInLineMethodsMap.keySet()){
            set.addAll(inlineClassFactory.classInLineMethodsMap.get(key));
        }
        return set;
    }

    def dealInLineClass(String patchPath) {
        //pay attention to order
        Set usedClass = new HashSet();
        usedClass.addAll(Config.newlyAddedClassNameList);
        Set newlyAddedClassInlineSet = getAllInlineClasses(usedClass, null);
        usedClass.addAll(newlyAddedClassInlineSet);
        usedClass.addAll(Config.modifiedClassNameList)
        Set inLineClassNameSet = getAllInlineClasses(usedClass, Config.patchMethodSignatureSet);
        inLineClassNameSet.removeAll(newlyAddedClassInlineSet)
        inLineClassNameSet.addAll(classInLineMethodsMap.keySet())
        //all inline patch class
        createHookInlineClass(inLineClassNameSet)
        //针对修改类的linepatch
        for (String fullClassName : inLineClassNameSet) {
            CtClass inlineClass = Config.classPool.get(fullClassName);
            List<String> inlineMethod = classInLineMethodsMap.getOrDefault(fullClassName, new ArrayList<String>());
            CtClass inlinePatchClass = PatchesFactory.createPatch(patchPath, inlineClass, true, NameManger.getInstance().getInlinePatchName(inlineClass.name), inlineMethod.toSet())
            inlinePatchClass.writeFile(patchPath)
        }
    }


    def dealInLineMethodInNewAddClass(String patchPath, List newAddClassList) {
        for (String fullClassName : newAddClassList) {
            CtClass newlyAddClass = Config.classPool.get(fullClassName);
            newlyAddClass.defrost();
            newlyAddClass.declaredMethods.each { method ->
                method.instrument(new ExprEditor() {
                    public void edit(MethodCall m) throws CannotCompileException {
                        repalceInlineMethod(m, method, true);
                    }
                })
            }
            newlyAddClass.writeFile(patchPath);
        }
    }

    def createHookInlineClass(Set inLineClassNameSet) {
        for (String fullClassName : inLineClassNameSet) {
            CtClass inlineClass = Config.classPool.get(fullClassName);
            CtClass inlinePatchClass = PatchesFactory.cloneClass(inlineClass, NameManger.getInstance().getInlinePatchName(inlineClass.name), null)
            inlinePatchClass = JavaUtils.addPatchConstruct(inlinePatchClass, inlineClass)
            PatchesFactory.createPublicMethodForPrivate(inlinePatchClass)
        }
    }
/***
 *
 * @param usedClass
 * @param patchMethodSignureSet 只查找指定的方法体来确认内联类，如果全部的类则传递null
 * @return
 */
    def Set getAllInlineClasses(Set usedClass, Set patchMethodSignureSet) {
        HashSet temInLineFirstSet = initInLineClass(usedClass, patchMethodSignureSet);
        HashSet temInLineSecondSet = initInLineClass(temInLineFirstSet, patchMethodSignureSet);
        temInLineSecondSet.addAll(temInLineFirstSet);
        //第一次temInLineFirstSet要和temInLineSecondSet第二次获取的内联类数量相同，则表明找出了所有的内联类
        while ((temInLineFirstSet.size() < temInLineSecondSet.size())) {
            temInLineFirstSet.addAll(initInLineClass(temInLineSecondSet, patchMethodSignureSet));
            //这个循环有点饶人，initInLineClass返回的是temInLineListSecond中所有的内联类
            temInLineSecondSet.addAll(initInLineClass(temInLineFirstSet, patchMethodSignureSet));
        }

        return temInLineSecondSet;
    }

    public static void dealInLineClass(String patchPath, List list) {
        inlineClassFactory.dealInLineClass(patchPath);
        inlineClassFactory.dealInLineMethodInNewAddClass(patchPath, list);
    }
    /**
     *
     * @param classNameList is modified class List
     * @return all inline classes used in classNameList
     */
    def HashSet initInLineClass(Set classNamesSet, Set patchMethodSignureSet) {
        HashSet inLineClassNameSet = new HashSet<String>();
        CtClass modifiedCtclass;
        Set <String>allPatchMethodSignureSet = new HashSet();
        boolean isNewClass=false;
        for (String fullClassName : classNamesSet) {
            if(patchMethodSignureSet!=null) {
                allPatchMethodSignureSet.addAll(patchMethodSignureSet);
            } else{
                isNewClass=true;
            }
            modifiedCtclass = Config.classPool.get(fullClassName)
            modifiedCtclass.declaredMethods.each {
                method ->
                    //找出modifiedclass中所有内联的类
                    allPatchMethodSignureSet.addAll(classInLineMethodsMap.getOrDefault(fullClassName, new ArrayList()))
                    if (isNewClass||allPatchMethodSignureSet.contains(method.longName)) {
//                        isNewClass=false;
                        method.instrument(new ExprEditor() {
                            @Override
                            void edit(MethodCall m) throws CannotCompileException {
                                List inLineMethodList = classInLineMethodsMap.getOrDefault(m.method.declaringClass.name, new ArrayList());
                                ClassMapping classMapping = ReadMapping.getInstance().getClassMapping(m.method.declaringClass.name);
                                if (null != classMapping && classMapping.memberMapping.get(ReflectUtils.getJavaMethodSignureWithReturnType(m.method)) == null) {
                                        inLineClassNameSet.add(m.method.declaringClass.name);
                                    if (!inLineMethodList.contains(m.method.longName)) {
                                        inLineMethodList.add(m.method.longName);
                                        classInLineMethodsMap.put(m.method.declaringClass.name, inLineMethodList)
                                    }
                                }
                            }
                        }
                        )
                    }
            }
        }
        return inLineClassNameSet;
    }


    def repalceInlineMethod(MethodCall m, CtMethod method, boolean isNewClass) {
        ClassMapping classMapping = ReadMapping.getInstance().getClassMapping(m.method.declaringClass.name);
        if (null != classMapping && classMapping.memberMapping.get(ReflectUtils.getJavaMethodSignureWithReturnType(m.method)) == null) {
            m.replace(ReflectUtils.getInLineMemberString(m.method, ReflectUtils.isStatic(method.modifiers), isNewClass));
            return true;
        }
        return false;
    }


}
