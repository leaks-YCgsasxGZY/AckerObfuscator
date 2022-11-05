package cn.AckerRun.Ackerobfuscator.transformer.misc;

import cn.AckerRun.Ackerobfuscator.transformer.*;
import cn.AckerRun.Ackerobfuscator.utils.pair.*;
import cn.AckerRun.Ackerobfuscator.*;
import java.lang.reflect.*;
import java.util.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import cn.AckerRun.Ackerobfuscator.utils.tree.*;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.tree.analysis.Frame;

public class InlinerTransformer extends Transformer
{
    private int maxPasses;
    private final boolean removal;
    private final boolean changeAccess;
    private final LinkedList<ClassMethodNode> inlinedMethods;
    private final LinkedList<ClassMethodNode> failed;
    private final Map<ClassMethodNode, Integer> passes;
    
    @Override
    public String getSection() {
        return "misc.inliner";
    }
    
    public InlinerTransformer(final AckerObfuscator obf) {
        super(obf);
        this.maxPasses = 0;
        this.inlinedMethods = new LinkedList<ClassMethodNode>();
        this.failed = new LinkedList<ClassMethodNode>();
        this.passes = new HashMap<ClassMethodNode, Integer>();
        this.maxPasses = this.config.getInt("maxPasses", 5);
        this.removal = this.config.getBoolean("remove-unused-methods", false);
        this.changeAccess = true;
    }
    
    @Override
    protected void visit(final ClassWrapper classNode) {
        this.passes.remove(this.target);
        boolean change;
        do {
            change = false;
            for (final MethodNode method : classNode.methods) {
                if (this.visitMethod(classNode, method)) {
                    change = true;
                }
            }
        } while (change);
    }
    
    public boolean visitMethod(final ClassWrapper classNode, final MethodNode method) {
        try {
            boolean change = false;
            final ClassMethodNode cmn = new ClassMethodNode(classNode, method);
            if (this.target == null || this.target.equals(cmn)) {
                this.passes.put(cmn, this.passes.getOrDefault(cmn, 0) + 1);
                if (this.passes.get(cmn) > this.maxPasses) {
                    return false;
                }
                for (final AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof MethodInsnNode && instruction.getOpcode() != 183 && instruction.getOpcode() != 185) {
                        final MethodInsnNode node = (MethodInsnNode)instruction;
                        final ClassWrapper owner = this.obf.assureLoaded(node.owner);
                        if (owner == null) {
                            continue;
                        }
                        final MethodNode target = AsmUtils.findMethodSuper(owner, node.name, node.desc);
                        if (target == null) {
                            continue;
                        }
                        if (Modifier.isAbstract(target.access)) {
                            continue;
                        }
                        if (Modifier.isNative(target.access)) {
                            continue;
                        }
                        final ClassMethodNode inline = new ClassMethodNode(owner, target);
                        if (this.failed.contains(inline) || !this.canInline(classNode, owner, target, false)) {
                            if (this.failed.contains(inline)) {
                                continue;
                            }
                            this.failed.add(inline);
                        }
                        else {
                            if (AsmUtils.codeSize(target) + AsmUtils.codeSize(method) >= 65535) {
                                continue;
                            }
                            this.inline(classNode, target, method, node);
                            this.log("Inlined %s.%s%s", owner.name, target.name, target.desc);
                            change = true;
                            final ClassMethodNode pair = new ClassMethodNode(owner, target);
                            if (this.inlinedMethods.contains(pair) || !owner.modify) {
                                continue;
                            }
                            this.inlinedMethods.add(pair);
                        }
                    }
                }
            }
            return change;
        }
        catch (Throwable $ex) {
            throw $ex;
        }
    }
    
    @Override
    protected void after() {
        for (final ClassWrapper classNode : this.obf.getClasses()) {
            this.run(classNode);
        }
        if (!this.removal) {
            return;
        }
        final LinkedList<ClassMethodNode> toRemove = new LinkedList<ClassMethodNode>(this.inlinedMethods);
        for (final ClassWrapper classNode2 : this.obf.getClasses()) {
            for (final MethodNode method : classNode2.methods) {
                for (final AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof MethodInsnNode) {
                        final ClassWrapper owner = this.obf.assureLoaded(((MethodInsnNode)instruction).owner);
                        if (owner == null) {
                            continue;
                        }
                        final MethodNode target = AsmUtils.findMethodSuper(owner, ((MethodInsnNode)instruction).name, ((MethodInsnNode)instruction).desc);
                        if (target == null) {
                            continue;
                        }
                        final ClassMethodNode classMethodNode = new ClassMethodNode(owner, target);
                        toRemove.removeIf(cmn -> cmn.equals(classMethodNode));
                    }
                }
            }
        }
        for (final ClassMethodNode classMethodNode2 : toRemove) {
            classMethodNode2.getClassWrapper().methods.remove(classMethodNode2.getMethodNode());
            this.log("Removed inlined method %s.%s%s", classMethodNode2.getClassWrapper().name, classMethodNode2.getMethodNode().name, classMethodNode2.getMethodNode().desc);
        }
    }
    
    public boolean canInline(final ClassWrapper ctx, final ClassWrapper classNode, final MethodNode method, final boolean debug) {
        if (this.excluded.contains(classNode.name + "." + method.name + method.desc)) {
            return false;
        }
        if (this.excluded.contains(method.name + method.desc)) {
            return false;
        }
        if (classNode.name.equals("java/lang/Object")) {
            return false;
        }
        if (method.instructions.size() <= 0) {
            return false;
        }
        for (final AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof FieldInsnNode) {
                final ClassWrapper ownerClass = this.obf.assureLoaded(((FieldInsnNode)instruction).owner);
                if (ownerClass == null) {
                    if (debug) {
                        this.error("[F] Could not find class %s", ((FieldInsnNode)instruction).owner);
                    }
                    return false;
                }
                if (!this.canAccess(ctx, ownerClass)) {
                    if (debug) {
                        this.error("Class %s is not accessible from %s", ownerClass.name, ctx.name);
                    }
                    return false;
                }
                final FieldNode field = AsmUtils.findFieldSuper(ownerClass, ((FieldInsnNode)instruction).name, ((FieldInsnNode)instruction).desc);
                if (field == null) {
                    if (debug) {
                        this.error("Could not find field %s.%s%s", ownerClass.name, ((FieldInsnNode)instruction).name, ((FieldInsnNode)instruction).desc);
                    }
                    return false;
                }
                final NodeAccess access = new NodeAccess(field.access);
                if (!this.checkAccess(access, classNode, ownerClass)) {
                    if (debug) {
                        this.error("Field %s.%s%s is not accessible", ownerClass.name, ((FieldInsnNode)instruction).name, ((FieldInsnNode)instruction).desc);
                    }
                    return false;
                }
                if (!this.checkAccess(access, ctx, ownerClass)) {
                    if (debug) {
                        this.error("Field %s.%s%s is not accessible", ownerClass.name, ((FieldInsnNode)instruction).name, ((FieldInsnNode)instruction).desc);
                    }
                    return false;
                }
                field.access = access.access;
            }
            else if (instruction instanceof TypeInsnNode) {
                String type = ((TypeInsnNode)instruction).desc;
                if (type.startsWith("[")) {
                    if (type.substring(type.lastIndexOf(91) + 1).length() == 1) {
                        continue;
                    }
                }
                else if (type.length() == 1) {
                    continue;
                }
                if (instruction.getOpcode() == 192 && type.startsWith("L") && type.endsWith(";")) {
                    type = type.substring(1, type.length() - 1);
                }
                final ClassWrapper ownerClass2 = this.obf.assureLoaded(type);
                if (ownerClass2 == null) {
                    if (debug) {
                        this.error("[T] Could not find class %s, opcode %d", type);
                    }
                    return false;
                }
                if (!this.canAccess(ctx, ownerClass2)) {
                    if (debug) {
                        this.error("Class %s is not accessible from %s", ownerClass2.name, ctx.name);
                    }
                    return false;
                }
                continue;
            }
            else if (instruction instanceof MethodInsnNode) {
                final MethodInsnNode node = (MethodInsnNode)instruction;
                final String owner = node.owner;
                final String name = node.name;
                final String desc = node.desc;
                if (node.getOpcode() == 183 && !name.equals("<init>") && !owner.equals(ctx.name)) {
                    if (debug) {
                        this.error("Cannot call super %s.%s%s from %s", owner, name, desc, ctx.name);
                    }
                    return false;
                }
                final ClassWrapper ownerClass3 = this.obf.assureLoaded(owner);
                if (ownerClass3 == null) {
                    if (debug) {
                        this.error("[M] Could not find class %s [%s.%s%s]", owner, owner, name, desc);
                    }
                    return false;
                }
                if (!this.canAccess(ctx, ownerClass3)) {
                    if (debug) {
                        this.error("Class %s is not accessible from %s", ownerClass3.name, ctx.name);
                    }
                    return false;
                }
                final MethodNode methodNode = AsmUtils.findMethodSuper(ownerClass3, name, desc);
                if (methodNode == null) {
                    if (debug) {
                        this.error("Could not find method %s.%s%s", owner, name, desc);
                    }
                    return false;
                }
                final NodeAccess access2 = new NodeAccess(methodNode.access);
                if (!this.checkAccess(access2, classNode, ownerClass3)) {
                    if (debug) {
                        this.error("Method %s.%s%s is not accessible", ownerClass3.name, name, desc);
                    }
                    return false;
                }
                if (!this.checkAccess(access2, ctx, ownerClass3)) {
                    if (debug) {
                        this.error("Method %s.%s%s is not accessible", ownerClass3.name, name, desc);
                    }
                    return false;
                }
                methodNode.access = access2.access;
            }
            else {
                if (!(instruction instanceof InvokeDynamicInsnNode)) {
                    continue;
                }
                final InvokeDynamicInsnNode node2 = (InvokeDynamicInsnNode)instruction;
                final String owner = node2.bsm.getOwner();
                final String name = node2.bsm.getName();
                final String desc = node2.bsm.getDesc();
                final ClassWrapper ownerClass3 = this.obf.assureLoaded(owner);
                if (ownerClass3 == null) {
                    if (debug) {
                        this.error("[INDY] Could not find class %s [%s.%s%s]", owner, owner, name, desc);
                    }
                    return false;
                }
                if (!this.canAccess(ctx, ownerClass3)) {
                    if (debug) {
                        this.error("[INDY] Class %s is not accessible from %s", ownerClass3.name, ctx.name);
                    }
                    return false;
                }
                final MethodNode methodNode = AsmUtils.findMethodSuper(ownerClass3, name, desc);
                if (methodNode == null) {
                    if (debug) {
                        this.error("[INDY] Could not find method %s.%s%s", owner, name, desc);
                    }
                    return false;
                }
                final NodeAccess access2 = new NodeAccess(methodNode.access);
                if (!this.checkAccess(access2, classNode, ownerClass3)) {
                    if (debug) {
                        this.error("[INDY] Method %s.%s%s is not accessible", ownerClass3.name, name, desc);
                    }
                    return false;
                }
                if (!this.checkAccess(access2, ctx, ownerClass3)) {
                    if (debug) {
                        this.error("[INDY] Method %s.%s%s is not accessible", ownerClass3.name, name, desc);
                    }
                    return false;
                }
                methodNode.access = access2.access;
            }
        }
        return true;
    }
    
    private boolean canAccess(final ClassWrapper ctx, final ClassWrapper ownerClass) {
        if (ctx.name.equals(ownerClass.name)) {
            return true;
        }
        if (Modifier.isPrivate(ownerClass.access)) {
            return false;
        }
        final String pkg1 = ctx.name.contains("/") ? ctx.name.substring(0, ctx.name.lastIndexOf(47)) : "";
        final String pkg2 = ownerClass.name.contains("/") ? ownerClass.name.substring(0, ownerClass.name.lastIndexOf(47)) : "";
        if (!Modifier.isPublic(ownerClass.access) && pkg1.equals(pkg2)) {
            return true;
        }
        if (!Modifier.isPublic(ownerClass.access) && ownerClass.modify && this.changeAccess) {
            ownerClass.access = this.fixAccess(ownerClass.access);
            return this.canAccess(ctx, ownerClass);
        }
        return Modifier.isPublic(ownerClass.access);
    }
    
    private int fixAccess(int access) {
        if (Modifier.isProtected(access)) {
            access &= 0xFFFFFFFB;
        }
        else if (Modifier.isPrivate(access)) {
            access &= 0xFFFFFFFD;
        }
        access |= 0x1;
        return access;
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
        if (!Modifier.isPrivate(acc)) {
            final String pkg1 = AsmUtils.parentName(classNode.name);
            final String pkg2 = AsmUtils.parentName(ownerClass.name);
            if (pkg1.equals(pkg2)) {
                return true;
            }
        }
        if (ownerClass.modify) {
            access.access = this.fixAccess(acc);
            return this.checkAccess(access, classNode, ownerClass);
        }
        return false;
    }
    
    public void inline(final ClassWrapper ctx, final MethodNode targetMethod, final MethodNode ctxMethod, final AbstractInsnNode target) {
        InsnList toInline = targetMethod.instructions;
        final InsnList toInlineInto = ctxMethod.instructions;
        final InsnList restore = new InsnList();
        final InsnList save = new InsnList();
        final Map<LabelNode, LabelNode> labels = new HashMap<LabelNode, LabelNode>();
        for (final AbstractInsnNode abstractInsnNode : toInline) {
            if (abstractInsnNode instanceof LabelNode) {
                final LabelNode labelNode = (LabelNode)abstractInsnNode;
                labels.put(labelNode, new LabelNode());
            }
        }
        for (final TryCatchBlockNode tryCatchBlock : targetMethod.tryCatchBlocks) {
            final TryCatchBlockNode cloned = new TryCatchBlockNode(labels.get(tryCatchBlock.start), labels.get(tryCatchBlock.end), labels.get(tryCatchBlock.handler), tryCatchBlock.type);
            ctxMethod.tryCatchBlocks.add(cloned);
        }
        final InsnList list = new InsnList();
        for (final AbstractInsnNode abstractInsnNode2 : toInline) {
            list.add(abstractInsnNode2.clone(labels));
        }
        toInline = list;
        final LabelNode start = new LabelNode();
        final LabelNode end = new LabelNode();
        final InsnList list2 = new InsnList();
        int retVar = -1;
        final Type retType = Type.getReturnType(targetMethod.desc);
        for (final AbstractInsnNode insn : toInline) {
            if (insn instanceof InsnNode && insn.getOpcode() >= 172 && insn.getOpcode() <= 177) {
                final InsnList list3 = new InsnList();
                if (insn.getOpcode() != 177) {
                    if (retVar == -1) {
                        retVar = targetMethod.maxLocals;
                        targetMethod.maxLocals += retType.getSize();
                    }
                    list3.add(new VarInsnNode(retType.getOpcode(54), retVar));
                }
                list3.add(new JumpInsnNode(167, end));
                toInline.insert(insn, list3);
                toInline.remove(insn);
            }
        }
        final Analyzer<BasicValue> analyzer = new Analyzer<BasicValue>(new BasicInterpreter());
        try {
            final Frame<BasicValue>[] frames = analyzer.analyzeAndComputeMaxs(ctx.name, ctxMethod);
            int argsStack = (target.getOpcode() != 184) ? 1 : 0;
            argsStack += Type.getArgumentTypes(targetMethod.desc).length;
            final Frame<BasicValue> frame = frames[target.index];
            if (frame != null) {
                int var = ctxMethod.maxLocals;
                for (int i = 0; i < frame.getStackSize() - argsStack; ++i) {
                    final Type type = frame.getStack(i).getType();
                    final InsnList sub = new InsnList();
                    sub.add(new VarInsnNode(type.getOpcode(54), var));
                    save.insert(sub);
                    restore.add(new VarInsnNode(type.getOpcode(21), var));
                    ++var;
                }
                ctxMethod.maxLocals = var;
            }
        }
        catch (Exception ex) {
            this.error("Failed to analyze method %s ", ex.getMessage());
        }
        list2.add(start);
        final MethodInsnNode methodInsn = (MethodInsnNode)target;
        final int locals = ctxMethod.maxLocals;
        final Type[] args = Type.getArgumentTypes(methodInsn.desc);
        final InsnList params = new InsnList();
        if (methodInsn.getOpcode() != 184) {
            params.insert(new VarInsnNode(58, ctxMethod.maxLocals++));
        }
        for (final Type arg : args) {
            params.insert(new VarInsnNode(arg.getOpcode(54), ctxMethod.maxLocals));
            ctxMethod.maxLocals += arg.getSize();
        }
        list2.add(params);
        list2.add(save);
        for (final AbstractInsnNode insn2 : toInline) {
            if (insn2 instanceof VarInsnNode) {
                final VarInsnNode varInsn = (VarInsnNode)insn2;
                if (varInsn.var == retVar) {
                    retVar += locals;
                }
                final VarInsnNode varInsnNode = varInsn;
                varInsnNode.var += locals;
            }
            else {
                if (!(insn2 instanceof IincInsnNode)) {
                    continue;
                }
                final IincInsnNode iincInsnNode2;
                final IincInsnNode iincInsnNode = iincInsnNode2 = (IincInsnNode)insn2;
                iincInsnNode2.var += locals;
            }
        }
        list2.add(toInline);
        list2.add(end);
        list2.add(restore);
        if (retVar != -1) {
            list2.add(new VarInsnNode(retType.getOpcode(21), retVar));
        }
        toInlineInto.insert(target, list2);
        toInlineInto.remove(target);
    }
}
