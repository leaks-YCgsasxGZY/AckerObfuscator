package cn.AckerRun.Ackerobfuscator.transformer.methods;

import cn.AckerRun.Ackerobfuscator.transformer.*;
import cn.AckerRun.Ackerobfuscator.*;
import org.apache.commons.lang3.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import java.util.*;
import org.objectweb.asm.*;
import java.lang.reflect.*;
import cn.AckerRun.Ackerobfuscator.utils.tree.*;

public class DynamicTransformer extends Transformer
{
    private static String handlerName;
    
    public DynamicTransformer(final AckerObfuscator obf) {
        super(obf);
    }
    
    @Override
    public String getSection() {
        return "methods.dynamic";
    }
    
    @Override
    protected void visit(final ClassWrapper classNode) {
        while (DynamicTransformer.handlerName == null) {
            for (final ClassWrapper cn : new ArrayList<ClassWrapper>(this.obf.getClasses())) {
                if (this.nextBoolean(5)) {
                    DynamicTransformer.handlerName = AsmUtils.parentName(cn.name) + RandomStringUtils.randomAlphabetic(3);
                    if (this.obf.assureLoaded(DynamicTransformer.handlerName) != null) {
                        DynamicTransformer.handlerName = null;
                    }
                    else {
                        final ClassWrapper handlerClass = new ClassWrapper(false);
                        handlerClass.visit(52, 1, DynamicTransformer.handlerName, null, "java/lang/Object", null);
                        MethodVisitor methodVisitor = handlerClass.visitMethod(1, "<init>", "()V", null, null);
                        methodVisitor.visitCode();
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitMethodInsn(183, "java/lang/Object", "<init>", "()V");
                        methodVisitor.visitInsn(177);
                        methodVisitor.visitEnd();
                        methodVisitor = handlerClass.visitMethod(137, "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;", null, null);
                        methodVisitor.visitCode();
                        final Label label0 = new Label();
                        final Label label2 = new Label();
                        final Label label3 = new Label();
                        methodVisitor.visitTryCatchBlock(label0, label2, label3, "java/lang/Throwable");
                        methodVisitor.visitLabel(label0);
                        methodVisitor.visitLineNumber(15, label0);
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(6);
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(6);
                        methodVisitor.visitInsn(50);
                        methodVisitor.visitTypeInsn(192, "java/lang/invoke/MethodHandle");
                        methodVisitor.visitMethodInsn(182, "java/lang/invoke/MethodHandle", "invoke", "()Ljava/lang/String;", false);
                        methodVisitor.visitInsn(83);
                        final Label label4 = new Label();
                        methodVisitor.visitLabel(label4);
                        methodVisitor.visitLineNumber(16, label4);
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(5);
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(5);
                        methodVisitor.visitInsn(50);
                        methodVisitor.visitTypeInsn(192, "java/lang/invoke/MethodHandle");
                        methodVisitor.visitMethodInsn(182, "java/lang/invoke/MethodHandle", "invoke", "()Ljava/lang/String;", false);
                        methodVisitor.visitInsn(83);
                        final Label label5 = new Label();
                        methodVisitor.visitLabel(label5);
                        methodVisitor.visitLineNumber(17, label5);
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(7);
                        methodVisitor.visitInsn(50);
                        methodVisitor.visitTypeInsn(192, "java/lang/invoke/MethodHandle");
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(8);
                        methodVisitor.visitInsn(50);
                        methodVisitor.visitTypeInsn(192, "java/lang/invoke/MethodHandle");
                        methodVisitor.visitInsn(7);
                        methodVisitor.visitTypeInsn(189, "java/lang/Object");
                        methodVisitor.visitInsn(89);
                        methodVisitor.visitInsn(3);
                        methodVisitor.visitMethodInsn(184, "java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", false);
                        methodVisitor.visitInsn(83);
                        methodVisitor.visitInsn(89);
                        methodVisitor.visitInsn(4);
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(4);
                        methodVisitor.visitInsn(50);
                        methodVisitor.visitInsn(83);
                        methodVisitor.visitInsn(89);
                        methodVisitor.visitInsn(5);
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(5);
                        methodVisitor.visitInsn(50);
                        methodVisitor.visitInsn(83);
                        methodVisitor.visitInsn(89);
                        methodVisitor.visitInsn(6);
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(6);
                        methodVisitor.visitInsn(50);
                        final Label label6 = new Label();
                        methodVisitor.visitLabel(label6);
                        methodVisitor.visitLineNumber(18, label6);
                        methodVisitor.visitMethodInsn(182, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(4);
                        methodVisitor.visitInsn(50);
                        methodVisitor.visitTypeInsn(192, "java/lang/Class");
                        methodVisitor.visitMethodInsn(182, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
                        methodVisitor.visitMethodInsn(184, "java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
                        methodVisitor.visitInsn(83);
                        final Label label7 = new Label();
                        methodVisitor.visitLabel(label7);
                        methodVisitor.visitLineNumber(17, label7);
                        methodVisitor.visitMethodInsn(182, "java/lang/invoke/MethodHandle", "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;", false);
                        methodVisitor.visitMethodInsn(182, "java/lang/invoke/MethodHandle", "bindTo", "(Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;", false);
                        methodVisitor.visitVarInsn(25, 0);
                        methodVisitor.visitInsn(3);
                        methodVisitor.visitInsn(50);
                        final Label label8 = new Label();
                        methodVisitor.visitLabel(label8);
                        methodVisitor.visitLineNumber(18, label8);
                        methodVisitor.visitMethodInsn(182, "java/lang/invoke/MethodHandle", "invoke", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                        methodVisitor.visitLabel(label2);
                        methodVisitor.visitLineNumber(17, label2);
                        methodVisitor.visitInsn(176);
                        methodVisitor.visitLabel(label3);
                        methodVisitor.visitLineNumber(12, label3);
                        methodVisitor.visitVarInsn(58, 1);
                        final Label label9 = new Label();
                        methodVisitor.visitLabel(label9);
                        methodVisitor.visitVarInsn(25, 1);
                        methodVisitor.visitInsn(191);
                        final Label label10 = new Label();
                        methodVisitor.visitLabel(label10);
                        methodVisitor.visitEnd();
                        this.obf.getClasses().add(handlerClass);
                    }
                }
            }
        }
        for (final MethodNode method : classNode.methods) {
            for (final AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode) {
                    final MethodInsnNode node = (MethodInsnNode)instruction;
                    final ClassWrapper owner = this.obf.assureLoaded(node.owner);
                    if (owner == null) {
                        continue;
                    }
                    NodeAccess access = new NodeAccess(owner.access);
                    if (!this.checkAccess(access, this.obf.assureLoaded(DynamicTransformer.handlerName), owner)) {
                        continue;
                    }
                    final MethodNode target = AsmUtils.findMethodSuper(owner, node.name, node.desc);
                    if (target == null) {
                        continue;
                    }
                    access = new NodeAccess(target.access);
                    if (!this.checkAccess(access, this.obf.assureLoaded(DynamicTransformer.handlerName), owner)) {
                        continue;
                    }
                    target.access = access.access;
                    if (node.getOpcode() == 184) {
                        final InsnList list = new InsnList();
                        final Type[] args = Type.getArgumentTypes(node.desc);
                        final InsnList stack = this.storeStack(false, args);
                        if (stack == null) {
                            continue;
                        }
                        list.add(stack);
                        list.add(AsmUtils.pushInt(6));
                        list.add(new TypeInsnNode(189, "java/lang/Object"));
                        list.add(new InsnNode(90));
                        list.add(new InsnNode(95));
                        list.add(AsmUtils.pushInt(0));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new InsnNode(89));
                        list.add(new LdcInsnNode(Type.getType("L" + node.owner + ";")));
                        list.add(AsmUtils.pushInt(1));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new InsnNode(89));
                        list.add(this.methodHandle(6, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false));
                        list.add(new LdcInsnNode(node.name));
                        list.add(new MethodInsnNode(182, "java/lang/invoke/MethodHandle", "bindTo", "(Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;", false));
                        list.add(AsmUtils.pushInt(2));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new InsnNode(89));
                        list.add(this.methodHandle(6, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false));
                        list.add(new LdcInsnNode(node.desc));
                        list.add(new MethodInsnNode(182, "java/lang/invoke/MethodHandle", "bindTo", "(Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;", false));
                        list.add(AsmUtils.pushInt(3));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new InsnNode(89));
                        list.add(this.methodHandle(5, "java/lang/invoke/MethodHandle", "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;", false));
                        list.add(AsmUtils.pushInt(4));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new InsnNode(89));
                        list.add(this.methodHandle(5, "java/lang/invoke/MethodHandles$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false));
                        list.add(AsmUtils.pushInt(5));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new MethodInsnNode(184, DynamicTransformer.handlerName, "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;"));
                        final Type returnType = Type.getReturnType(node.desc);
                        if (returnType.equals(Type.VOID_TYPE)) {
                            list.add(new InsnNode(87));
                        }
                        else {
                            AsmUtils.unboxPrimitive(returnType.getDescriptor(), list);
                        }
                        method.instructions.insert(instruction, list);
                        method.instructions.remove(instruction);
                    }
                    else {
                        if (node.getOpcode() != 182 && node.getOpcode() != 185) {
                            continue;
                        }
                        final InsnList list = new InsnList();
                        final Type[] args = Type.getArgumentTypes(node.desc);
                        final InsnList stack = this.storeStack(true, args);
                        if (stack == null) {
                            continue;
                        }
                        list.add(stack);
                        list.add(AsmUtils.pushInt(6));
                        list.add(new TypeInsnNode(189, "java/lang/Object"));
                        list.add(new InsnNode(90));
                        list.add(new InsnNode(95));
                        list.add(AsmUtils.pushInt(0));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new InsnNode(89));
                        list.add(new LdcInsnNode(Type.getType("L" + node.owner + ";")));
                        list.add(AsmUtils.pushInt(1));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new InsnNode(89));
                        list.add(this.methodHandle(6, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false));
                        list.add(new LdcInsnNode(node.name));
                        list.add(new MethodInsnNode(182, "java/lang/invoke/MethodHandle", "bindTo", "(Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;", false));
                        list.add(AsmUtils.pushInt(2));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new InsnNode(89));
                        list.add(this.methodHandle(6, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false));
                        list.add(new LdcInsnNode(node.desc));
                        list.add(new MethodInsnNode(182, "java/lang/invoke/MethodHandle", "bindTo", "(Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;", false));
                        list.add(AsmUtils.pushInt(3));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new InsnNode(89));
                        list.add(this.methodHandle(5, "java/lang/invoke/MethodHandle", "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;", false));
                        list.add(AsmUtils.pushInt(4));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new InsnNode(89));
                        list.add(this.methodHandle(5, "java/lang/invoke/MethodHandles$Lookup", "findVirtual", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false));
                        list.add(AsmUtils.pushInt(5));
                        list.add(new InsnNode(95));
                        list.add(new InsnNode(83));
                        list.add(new MethodInsnNode(184, DynamicTransformer.handlerName, "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;"));
                        final Type returnType = Type.getReturnType(node.desc);
                        if (returnType.equals(Type.VOID_TYPE)) {
                            list.add(new InsnNode(87));
                        }
                        else {
                            AsmUtils.unboxPrimitive(returnType.getDescriptor(), list);
                        }
                        method.instructions.insert(instruction, list);
                        method.instructions.remove(instruction);
                    }
                }
            }
        }
    }
    
    private InsnList methodHandle(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        final InsnList list = new InsnList();
        list.add(new LdcInsnNode(new Handle(opcode, owner, name, desc, itf)));
        return list;
    }
    
    private boolean checkAccess(final NodeAccess access, final ClassWrapper classNode, final ClassWrapper ownerClass) {
        final int acc = access.access;
        if (Modifier.isPublic(acc)) {
            return true;
        }
        if (!Modifier.isPublic(acc) && classNode.name.equals(ownerClass.name)) {
            return true;
        }
        if (Modifier.isProtected(acc)) {
            final String parent = ownerClass.name;
            if (HierarchyUtils.isAssignableFrom(parent, classNode.name)) {
                return true;
            }
        }
        if (!Modifier.isPrivate(acc) && (0x1000 & acc) == 0x0) {
            final String pkg1 = AsmUtils.parentName(classNode.name);
            final String pkg2 = AsmUtils.parentName(ownerClass.name);
            return pkg1.equals(pkg2);
        }
        return false;
    }
    
    private InsnList storeStack(final boolean virtual, final Type[] types) {
        final InsnList list = new InsnList();
        final Type[] args = new Type[types.length + (virtual ? 1 : 0)];
        for (final Type type : types) {
            if (type.getDescriptor().startsWith("[")) {
                final String actual = type.getDescriptor().substring(type.getDescriptor().lastIndexOf(91) + 1);
                if (actual.equals("Ljava/lang/Object;")) {
                    return null;
                }
            }
        }
        System.arraycopy(types, 0, args, virtual ? 1 : 0, types.length);
        if (virtual) {
            args[0] = Type.getType("Ljava/lang/Object;");
        }
        list.add(AsmUtils.pushInt(args.length));
        list.add(new TypeInsnNode(189, "java/lang/Object"));
        for (int i = args.length - 1; i >= 0; --i) {
            final Type arg = args[i];
            final InsnList sub = new InsnList();
            if (arg.getSize() > 1) {
                sub.add(new InsnNode(91));
                sub.add(new InsnNode(91));
                sub.add(new InsnNode(87));
                sub.add(AsmUtils.pushInt(i));
                sub.add(new InsnNode(91));
                sub.add(new InsnNode(87));
            }
            else {
                sub.add(new InsnNode(90));
                sub.add(new InsnNode(95));
                sub.add(AsmUtils.pushInt(i));
                sub.add(new InsnNode(95));
            }
            AsmUtils.boxPrimitive(arg.getDescriptor(), sub);
            sub.add(new InsnNode(83));
            list.add(sub);
        }
        return list;
    }
}
