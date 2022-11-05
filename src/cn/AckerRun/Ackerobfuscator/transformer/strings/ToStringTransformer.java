package cn.AckerRun.Ackerobfuscator.transformer.strings;

import cn.AckerRun.Ackerobfuscator.transformer.*;
import cn.AckerRun.Ackerobfuscator.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import org.apache.commons.lang3.*;
import org.objectweb.asm.tree.*;
import java.util.*;
import org.objectweb.asm.*;

public class ToStringTransformer extends Transformer
{
    public ToStringTransformer(final AckerObfuscator obf) {
        super(obf);
    }
    
    @Override
    public String getSection() {
        return "strings.tostring";
    }
    
    public void visit(final ClassWrapper classNode) {
        for (final MethodNode method : classNode.methods) {
            for (final AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof LdcInsnNode && ((LdcInsnNode)instruction).cst instanceof String) {
                    final String string = (String)((LdcInsnNode)instruction).cst;
                    final ClassWrapper cn = new ClassWrapper(true);
                    cn.visit(52, 48, AsmUtils.parentName(classNode.name) + RandomStringUtils.randomAlphabetic(5), null, "java/lang/Object", null);
                    MethodVisitor mn = cn.visitMethod(1, "toString", "()Ljava/lang/String;", null, null);
                    mn.visitCode();
                    final char[] keys = new char[string.length()];
                    final char[] chars = new char[string.length()];
                    for (int i = 0; i < keys.length; ++i) {
                        keys[i] = (char)(string.charAt(i) ^ Arrays.hashCode(chars));
                        chars[i] = string.charAt(i);
                    }
                    mn.visitLdcInsn(keys.length);
                    mn.visitIntInsn(188, 5);
                    mn.visitVarInsn(58, 1);
                    final Label l = new Label();
                    mn.visitLabel(l);
                    for (int j = 0; j < keys.length; ++j) {
                        mn.visitVarInsn(25, 1);
                        mn.visitInsn(89);
                        mn.visitLdcInsn(j);
                        mn.visitLdcInsn((int)keys[j]);
                        mn.visitVarInsn(25, 1);
                        mn.visitMethodInsn(184, "java/util/Arrays", "hashCode", "([C)I", false);
                        mn.visitInsn(146);
                        mn.visitInsn(146);
                        mn.visitInsn(130);
                        mn.visitInsn(85);
                        mn.visitVarInsn(58, 1);
                    }
                    mn.visitTypeInsn(187, "java/lang/String");
                    mn.visitInsn(89);
                    mn.visitVarInsn(25, 1);
                    mn.visitMethodInsn(183, "java/lang/String", "<init>", "([C)V", false);
                    mn.visitInsn(176);
                    mn.visitEnd();
                    mn = cn.visitMethod(0, "<init>", "()V", null, null);
                    mn.visitCode();
                    mn.visitVarInsn(25, 0);
                    mn.visitMethodInsn(183, "java/lang/Object", "<init>", "()V", false);
                    mn.visitInsn(177);
                    cn.visitOuterClass(classNode.name, method.name, method.desc);
                    cn.visitEnd();
                    classNode.innerClasses.add(new InnerClassNode(cn.name, null, null, 8));
                    final InsnList list = new InsnList();
                    list.add(new TypeInsnNode(187, cn.name));
                    list.add(new InsnNode(89));
                    list.add(new MethodInsnNode(183, cn.name, "<init>", "()V", false));
                    list.add(new MethodInsnNode(182, cn.name, "toString", "()Ljava/lang/String;", false));
                    method.instructions.insertBefore(instruction, list);
                    method.instructions.remove(instruction);
                    this.obf.addClass(cn);
                }
            }
        }
    }
}
