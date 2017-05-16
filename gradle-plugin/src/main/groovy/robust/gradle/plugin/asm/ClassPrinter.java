package robust.gradle.plugin.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;


/**
 * Created by zhangmeng on 2017/5/10.
 */

public class ClassPrinter extends ClassVisitor {
    public ClassPrinter() {
        super(Opcodes.ASM5);
    }
    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
//        System.out.println(" "+name + " extends " + superName + " {");
        System.out.println("visit ");
    }
    public void visitSource(String source, String debug) {
        System.out.println("visitSource source is "+source );
    }
    public void visitOuterClass(String owner, String name, String desc) {
        System.out.println("visitOuterClass " );
    }
    public AnnotationVisitor visitAnnotation(String desc,
                                             boolean visible) {
        System.out.println("visitAnnotation " );
        return null;
    }
    public void visitAttribute(Attribute attr) {
        System.out.println("visitAttribute " );
    }
    public void visitInnerClass(String name, String outerName,
                                String innerName, int access) {
        System.out.println("visitInnerClass name "+name+" outerName "+outerName +" innerName "+innerName);
    }
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        System.out.println( "visitField   "+desc + " " + name);
//        System.out.println("visitField " );
        return null;
    }
    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature, String[] exceptions) {
        System.out.println("visitMethod  " + name + desc);
//        System.out.println("visitMethod " );
        return null;
    }
    public void visitEnd() {
//        System.out.println("}");
        System.out.println("visitEnd ");
    }


    public static void  main(String []args) throws IOException {
        ClassPrinter cp = new ClassPrinter();
        ClassReader cr = new ClassReader("java.lang.String");
//        ClassReader cr = new ClassReader("java.lang.Runnable");
        cr.accept(cp, 0);
    }
}
