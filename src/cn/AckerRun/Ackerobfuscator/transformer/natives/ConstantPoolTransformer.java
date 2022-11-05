package cn.AckerRun.Ackerobfuscator.transformer.natives;

import cn.AckerRun.Ackerobfuscator.transformer.*;
import cn.AckerRun.Ackerobfuscator.*;
import org.objectweb.asm.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;

public class ConstantPoolTransformer extends Transformer
{
    public ConstantPoolTransformer(final AckerObfuscator obf) {
        super(obf);
        this.canBeIterated = false;
    }
    
    @Override
    public String getSection() {
        return "natives.constantpool";
    }
    
    @Override
    protected void after() {
        try {
            for (final ClassWrapper classNode : this.obf.getClasses()) {
                if (classNode.name.equals("vm/NativeHandler")) {
                    continue;
                }
                this.transform(classNode);
            }
        }
        catch (Throwable $ex) {
            throw $ex;
        }
    }
    
    void transform(final ClassWrapper classNode) {
        final InsnList list = new InsnList();
        list.add(new LdcInsnNode(Type.getType("L" + classNode.name + ";")));
        list.add(new MethodInsnNode(184, "vm/NativeHandler", "decryptConstantPool", "(Ljava/lang/Class;)V", false));
        AsmUtils.getClinit(classNode).instructions.insert(list);
        final int key = classNode.name.hashCode();
        for (int i = 0; i < 2; ++i) {
            for (final MethodNode method : classNode.methods) {
                for (final AbstractInsnNode instruction : method.instructions) {
                    if (AsmUtils.isPushInt(instruction)) {
                        final int value = AsmUtils.getPushedInt(instruction);
                        final InsnList list2 = new InsnList();
                        final int r1 = this.random.nextInt();
                        list2.add(new LdcInsnNode((Object)r1));
                        list2.add(new LdcInsnNode((Object)(value ^ r1)));
                        list2.add(new InsnNode(130));
                        method.instructions.insert(instruction, list2);
                        method.instructions.remove(instruction);
                    }
                    else {
                        if (!AsmUtils.isPushLong(instruction)) {
                            continue;
                        }
                        final long value2 = AsmUtils.getPushedLong(instruction);
                        final InsnList list3 = new InsnList();
                        final long r2 = this.random.nextLong();
                        list3.add(new LdcInsnNode(r2));
                        list3.add(new LdcInsnNode(value2 ^ r2));
                        list3.add(new InsnNode(131));
                        method.instructions.insert(instruction, list3);
                        method.instructions.remove(instruction);
                    }
                }
            }
        }
        for (final MethodNode method2 : classNode.methods) {
            for (final AbstractInsnNode instruction2 : method2.instructions) {
                if (AsmUtils.isPushInt(instruction2)) {
                    int value3 = AsmUtils.getPushedInt(instruction2);
                    value3 -= key << 2;
                    value3 ^= key;
                    method2.instructions.set(instruction2, new LdcInsnNode((Object)value3));
                }
                else if (AsmUtils.isPushLong(instruction2)) {
                    long value4 = AsmUtils.getPushedLong(instruction2);
                    value4 ^= key << 4;
                    value4 -= key;
                    method2.instructions.set(instruction2, new LdcInsnNode(value4));
                }
                else {
                    if (!(instruction2 instanceof LdcInsnNode)) {
                        continue;
                    }
                    final Object value5 = ((LdcInsnNode)instruction2).cst;
                    if (value5 instanceof Float) {
                        final float f = (float)value5;
                        method2.instructions.set(instruction2, new LdcInsnNode(Math.pow(f, 3.0)));
                    }
                    else {
                        if (!(value5 instanceof Double)) {
                            continue;
                        }
                        final double d = (double)value5;
                        method2.instructions.set(instruction2, new LdcInsnNode(Math.pow(d, 3.0)));
                    }
                }
            }
        }
    }
}
