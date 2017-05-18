package robust.gradle.plugin.asm;

import com.meituan.robust.ChangeQuickRedirect;
import com.meituan.robust.Constants;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhangmeng on 2017/5/10.
 */

public class InsertMethodBodyAdapter extends ClassVisitor implements Opcodes {

    public InsertMethodBodyAdapter() {
        super(Opcodes.ASM5);
    }
    ClassWriter classWriter;
    private String className;
    //this maybe change in the future
    private String methodId;
    public InsertMethodBodyAdapter(ClassWriter cw,String className,String methodId) {
        super(Opcodes.ASM5,cw);
        this.classWriter =cw;
        this.className=className;
        this.methodId=methodId;
        classWriter.visitField(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, Constants.INSERT_FIELD_NAME, Type.getDescriptor(ChangeQuickRedirect.class), null, null);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!name.equals("run"))
            return super.visitMethod(access, name,
                    desc, signature, exceptions);
        //
        MethodVisitor mv = super.visitMethod(access, name,
                desc, signature, exceptions);


        return new MethodBodyInsertor(mv,className,desc,isStatic(access), methodId,name,access);
    }
    class MethodBodyInsertor extends GeneratorAdapter implements Opcodes {
        private String className;
        private Type[] argsType;
        private Type returnType;
        List<Type> paramsTypeClass=new ArrayList();
        boolean isStatic;
        //目前methodid是int类型的，未来可能会修改为String类型的，这边进行了一次强转
        String methodId;


        public MethodBodyInsertor(MethodVisitor mv,String className, String desc, boolean isStatic,String methodId,String name,int access) {
            super(Opcodes.ASM5, mv, access, name, desc);
            this.className=className;
            this.returnType =Type.getReturnType(desc);
            Type[] argsType = Type.getArgumentTypes(desc);
            for (Type type : argsType) {
                paramsTypeClass.add(type);
            }
            this.isStatic=isStatic;
            this.methodId =methodId;
        }


        @Override
        public void visitCode() {
            RobustAsmUtils.createInsertCode(this,className,paramsTypeClass, returnType,isStatic,Integer.valueOf(methodId));
//            mv.visitInsn(ACONST_NULL);
//            mv.visitVarInsn(ASTORE, 2);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitVarInsn(ASTORE, 2);
//            mv.visitInsn(ICONST_1);
//            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
//            mv.visitInsn(DUP);
//            mv.visitInsn(ICONST_0);
//            mv.visitVarInsn(ALOAD, 1);
//            mv.visitInsn(AASTORE);
//            mv.visitVarInsn(ALOAD, 2);
//            //put current className
//            mv.visitFieldInsn(GETSTATIC, className, "changeQuickRedirect", "Lcom/meituan/robust/ChangeQuickRedirect;");
//            mv.visitInsn(ICONST_0);
//            //put methodid here
//            mv.visitIntInsn(BIPUSH, methodId);
//            mv.visitInsn(ICONST_1);
//            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
//            mv.visitInsn(DUP);
//            mv.visitInsn(ICONST_0);
//            mv.visitLdcInsn(Type.getType("[Ljava/lang/Object;"));
//            mv.visitInsn(AASTORE);
//            mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
//            mv.visitMethodInsn(INVOKESTATIC, "com/meituan/robust/PatchProxy", "isSupport", "([Ljava/lang/Object;Ljava/lang/Object;Lcom/meituan/robust/ChangeQuickRedirect;ZI[Ljava/lang/Class;Ljava/lang/Class;)Z");
//            Label l0 = new Label();
//            mv.visitJumpInsn(IFEQ, l0);
//            mv.visitInsn(ICONST_1);
//            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
//            mv.visitInsn(DUP);
//            mv.visitInsn(ICONST_0);
//            mv.visitVarInsn(ALOAD, 1);
//            mv.visitInsn(AASTORE);
//            mv.visitVarInsn(ALOAD, 2);
//            //put current className
//            mv.visitFieldInsn(GETSTATIC, className, "changeQuickRedirect", "Lcom/meituan/robust/ChangeQuickRedirect;");
//            mv.visitInsn(ICONST_0);
//            //put methodid here
//            mv.visitIntInsn(BIPUSH, methodId);
//            mv.visitInsn(ICONST_1);
//            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
//            mv.visitInsn(DUP);
//            mv.visitInsn(ICONST_0);
//            mv.visitLdcInsn(Type.getType("[Ljava/lang/Object;"));
//            mv.visitInsn(AASTORE);
//            mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
//            mv.visitMethodInsn(INVOKESTATIC, "com/meituan/robust/PatchProxy", "accessDispatch", "([Ljava/lang/Object;Ljava/lang/Object;Lcom/meituan/robust/ChangeQuickRedirect;ZI[Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/Object;");
//            mv.visitTypeInsn(CHECKCAST, "java/lang/Object");
//            mv.visitInsn(getReturnTypeCode(returnType));
//            mv.visitLabel(l0);
        }
        /**
         * 针对不同类型返回指令不一样
         * @param typeS
         * @return
         */
        private int getReturnTypeCode(String typeS){
            if("Z".equals(typeS)){
                return Opcodes.IRETURN;
            }
            if("B".equals(typeS)){
                return Opcodes.IRETURN;
            }
            if("C".equals(typeS)){
                return Opcodes.IRETURN;
            }
            if("S".equals(typeS)){
                return Opcodes.IRETURN;
            }
            if("I".equals(typeS)){
                return Opcodes.IRETURN;
            }
            if("F".equals(typeS)){
                return Opcodes.FRETURN;
            }
            if("D".equals(typeS)){
                return Opcodes.DRETURN;
            }
            if("J".equals(typeS)){
                return Opcodes.LRETURN;
            }
            return Opcodes.ARETURN;
        }
    }
    private boolean isStatic(int access){
        return (access & Opcodes.ACC_STATIC) != 0;
//        return Opcodes.ACC_STATIC == (access & Opcodes.ACC_STATIC);
    }

    public static void  main(String []args) throws IOException {
        InsertMethodBodyAdapter cp = new InsertMethodBodyAdapter();
        ClassReader cr = new ClassReader("java.lang.String");
        cr.accept(cp, 0);
    }

    public static byte []  deCode(byte []b1,String className,String methodId) throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        InsertMethodBodyAdapter insertMethodBodyAdapter=new InsertMethodBodyAdapter(cw,className,methodId);
        ClassReader cr = new ClassReader(b1);
        cr.accept(insertMethodBodyAdapter,ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

}
