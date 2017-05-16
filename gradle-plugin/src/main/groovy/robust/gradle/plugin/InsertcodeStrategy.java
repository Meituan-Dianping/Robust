package robust.gradle.plugin;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.expr.Cast;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.Handler;
import javassist.expr.Instanceof;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;

/**
 * Created by zhangmeng on 2017/5/10.
 */

public abstract class InsertcodeStrategy {
    protected abstract void inserCode();
    /**
     * 判断是否有方法调用
     * @return 是否插桩
     */
    boolean isCallMethod;
    public boolean modifyMethodCodeFilter(CtMethod ctMethod) throws CannotCompileException {

        if (ctMethod == null) {
            return false;
        }
        //判断代码中是否有方法调用

        ctMethod.instrument(new ExprEditor() {

            /**
             * Edits a <tt>new</tt> expression (overridable).
             * The default implementation performs nothing.
             *
             * @param e the <tt>new</tt> expression creating an object.
             */
            public void edit(NewExpr e) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits an expression for array creation (overridable).
             * The default implementation performs nothing.
             *
             * @param a the <tt>new</tt> expression for creating an array.
             * @throws CannotCompileException
             */
            public void edit(NewArray a) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits a method call (overridable).
             *
             * The default implementation performs nothing.
             */
            public void edit(MethodCall m) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits a constructor call (overridable).
             * The constructor call is either
             * <code>super()</code> or <code>this()</code>
             * included in a constructor body.
             *
             * The default implementation performs nothing.
             *
             * @see #edit(NewExpr)
             */
            public void edit(ConstructorCall c) throws CannotCompileException {
                isCallMethod = true;
            }

            /**
             * Edits an instanceof expression (overridable).
             * The default implementation performs nothing.
             */
            public void edit(Instanceof i) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits an expression for explicit type casting (overridable).
             * The default implementation performs nothing.
             */
            public void edit(Cast c) throws CannotCompileException { isCallMethod = true; }

            /**
             * Edits a catch clause (overridable).
             * The default implementation performs nothing.
             */
            public void edit(Handler h) throws CannotCompileException { isCallMethod = true; }
        });
        return isCallMethod;
    }
//    /**
//     * 根据传入类型判断调用PathProxy的方法
//     * @param type 返回类型
//     * @param isStatic 是否是静态方法
//     * @param methodNumber 方法数
//     * @return 返回return语句
//     */
//    public String getReturnStatement(String type, boolean isStatic, int methodNumber) {
//        switch (type) {
//            case Constants.CONSTRUCTOR:
//                return "    com.meituan.robust.PatchProxy.accessDispatchVoid(\$args, argThis, changeQuickRedirect, , $methodNumber);  "
//            case Constants.LANG_VOID:
//                return "    com.meituan.robust.PatchProxy.accessDispatchVoid(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber);   return null;"
//
//            case Constants.VOID:
//                return "    com.meituan.robust.PatchProxy.accessDispatchVoid(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber);   return ;"
//
//            case Constants.LANG_BOOLEAN:
//                return "   return ((java.lang.Boolean)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber));"
//            case Constants.BOOLEAN:
//                return "   return ((java.lang.Boolean)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber)).booleanValue();"
//
//            case Constants.INT:
//                return "   return ((java.lang.Integer)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber)).intValue();"
//            case Constants.LANG_INT:
//                return "   return ((java.lang.Integer)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber)); "
//
//            case Constants.LONG:
//                return "   return ((java.lang.Long)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber)).longValue();"
//            case Constants.LANG_LONG:
//                return "   return ((java.lang.Long)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber));"
//
//            case Constants.DOUBLE:
//                return "   return ((java.lang.Double)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber)).doubleValue();"
//            case Constants.LANG_DOUBLE:
//                return "   return ((java.lang.Double)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber));"
//
//            case Constants.FLOAT:
//                return "   return ((java.lang.Float)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber)).floatValue();"
//            case Constants.LANG_FLOAT:
//                return "   return ((java.lang.Float)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber));"
//
//            case Constants.SHORT:
//                return "   return ((java.lang.Short)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber)).shortValue();"
//            case Constants.LANG_SHORT:
//                return "   return ((java.lang.Short)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber));"
//
//            case Constants.BYTE:
//                return "   return ((java.lang.Byte)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber)).byteValue();"
//            case Constants.LANG_BYTE:
//                return "   return ((java.lang.Byte)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber));"
//            case Constants.CHAR:
//                return "   return ((java.lang.Character)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber)).charValue();"
//            case Constants.LANG_CHARACTER:
//                return "   return ((java.lang.Character)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber));"
//            default:
//                return "   return ($type)com.meituan.robust.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, $isStatic, $methodNumber);"
//        }
//    }

}
