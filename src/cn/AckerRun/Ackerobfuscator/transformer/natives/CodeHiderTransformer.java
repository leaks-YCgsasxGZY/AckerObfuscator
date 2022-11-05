package cn.AckerRun.Ackerobfuscator.transformer.natives;

import cn.AckerRun.Ackerobfuscator.transformer.*;
import cn.AckerRun.Ackerobfuscator.*;
import org.apache.commons.lang3.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class CodeHiderTransformer extends Transformer
{
    private final List<String> vmExcluded;
    
    public CodeHiderTransformer(final AckerObfuscator obf) {
        super(obf);
        this.vmExcluded = new ArrayList<String>();
        this.canBeIterated = false;
    }
    
    @Override
    public String getSection() {
        return "natives.codehider";
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
    
    public void transform(final ClassWrapper classNode) {
        try {
            this.vmExcluded.clear();
            final String _name = classNode.name + RandomStringUtils.randomNumeric(50);
            final String name = classNode.name;
            final String bytesCallName = "bytesCall$" + RandomStringUtils.randomNumeric(10);
            ContextClassWriter cw = new ContextClassWriter(2);
            classNode.name = _name;
            final Map<String, AbstractInsnNode[]> cached = new HashMap<String, AbstractInsnNode[]>();
            for (final MethodNode method : new ArrayList<MethodNode>(classNode.methods)) {
                if (!this.safe(method)) {
                    continue;
                }
                this.setup(classNode, method, name);
            }
            for (final MethodNode method : classNode.methods) {
                if (!method.name.equals("<clinit>")) {
                    continue;
                }
                final AbstractInsnNode[] instructions = method.instructions.toArray();
                cached.put(method.name + method.desc, instructions);
                final LabelNode endLabel = new LabelNode();
                final InsnList list = new InsnList();
                list.add(AsmUtils.pushInt((short)this.random.nextInt()));
                list.add(new InsnNode(3));
                list.add(new InsnNode(130));
                list.add(new JumpInsnNode(154, endLabel));
                method.instructions.insert(list);
                method.instructions.add(endLabel);
                method.instructions.add(new InsnNode(177));
            }
            final int old = classNode.access;
            classNode.access |= 0x1;
            final MethodNode bytesCall = new MethodNode(9, bytesCallName, "(Ljava/lang/Class;Ljava/lang/String;)[I", null, null);
            (bytesCall.instructions = new InsnList()).add(new VarInsnNode(25, 0));
            bytesCall.instructions.add(new VarInsnNode(25, 1));
            bytesCall.instructions.add(new MethodInsnNode(184, "vm/NativeHandler", "raw_bytes", "(Ljava/lang/Class;Ljava/lang/String;)[I"));
            bytesCall.instructions.add(new InsnNode(176));
            bytesCall.maxStack = 10;
            bytesCall.maxLocals = 10;
            classNode.methods.add(bytesCall);
            classNode.accept(cw);
            classNode.access = old;
            classNode.name = name;
            classNode.methods.remove(bytesCall);
            for (final MethodNode method2 : classNode.methods) {
                final String id = method2.name + method2.desc;
                if (cached.containsKey(id)) {
                    method2.instructions.clear();
                    final Stream<AbstractInsnNode> stream = Arrays.stream(cached.get(id));
                    final InsnList instructions2 = method2.instructions;
                    Objects.requireNonNull(instructions2);
                    stream.forEach(instructions2::add);
                }
            }
            this.obf.getLoader().addClass(_name, cw.toByteArray());
            for (final MethodNode method3 : new ArrayList<MethodNode>(classNode.methods)) {
                if (!this.safe(method3)) {
                    this.vmExcluded.add(method3.name + method3.desc);
                }
                else {
                    this.log("%s.%s%s", classNode.name, method3.name, method3.desc);
                    int[] bytes;
                    try {
                        final Class<?> klass = this.obf.getLoader().loadClass(_name.replace('/', '.'), true);
                        bytes = (int[])klass.getMethod(bytesCallName, Class.class, String.class).invoke(null, klass, method3.name + method3.desc);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        this.vmExcluded.add(method3.name + method3.desc);
                        continue;
                    }
                    this.registerMethod(classNode, method3, bytes);
                }
            }
            cw = new ContextClassWriter(2, true, this.vmExcluded);
            classNode.accept(cw);
            this.obf.addGeneratedClass(classNode.name, cw.toByteArray());
        }
        catch (Throwable $ex) {
            throw $ex;
        }
    }
    
    public void setup(final ClassWrapper classNode, final MethodNode method, final String realName) {
        final MethodNode clinit = AsmUtils.getClinit(classNode);
        final MethodNode methodNode = new MethodNode(8, "setup$" + Math.abs(realName.hashCode() + classNode.superName.hashCode() + method.name.hashCode() + method.desc.hashCode()), "()V", null, null);
        this.vmExcluded.add(methodNode.name + methodNode.desc);
        final InsnList list = new InsnList();
        list.add(new LdcInsnNode(Type.getType("L" + realName + ";")));
        list.add(new LdcInsnNode(method.name + method.desc));
        list.add(new InsnNode(3));
        list.add(new IntInsnNode(188, 10));
        list.add(new MethodInsnNode(184, "vm/NativeHandler", "transformMethod", "(Ljava/lang/Class;Ljava/lang/String;[I)V", false));
        list.add(new InsnNode(177));
        methodNode.instructions = list;
        classNode.methods.add(methodNode);
        AbstractInsnNode start = null;
        for (final AbstractInsnNode instruction : clinit.instructions) {
            if (instruction instanceof MethodInsnNode) {
                final MethodInsnNode node = (MethodInsnNode)instruction;
                if (node.name.equals("decryptConstantPool")) {
                    start = instruction;
                    break;
                }
                continue;
            }
        }
        if (start != null) {
            clinit.instructions.insert(start, new MethodInsnNode(184, realName, methodNode.name, methodNode.desc, false));
        }
        else {
            clinit.instructions.insert(new MethodInsnNode(184, realName, methodNode.name, methodNode.desc, false));
        }
    }
    
    public void registerMethod(final ClassWrapper classNode, final MethodNode method, final int[] code) {
        final InsnList list = new InsnList();
        list.add(AsmUtils.pushInt(code.length));
        list.add(new IntInsnNode(188, 10));
        for (int i = 0; i < code.length; ++i) {
            list.add(new InsnNode(89));
            list.add(AsmUtils.pushInt(i));
            list.add(AsmUtils.pushInt((byte)code[i]));
            list.add(new InsnNode(79));
        }
        final MethodNode mn = AsmUtils.findMethod(classNode, "setup$" + Math.abs(classNode.name.hashCode() + classNode.superName.hashCode() + method.name.hashCode() + method.desc.hashCode()), "()V");
        for (final AbstractInsnNode instruction : mn.instructions) {
            if (instruction instanceof IntInsnNode) {
                method.instructions.insert(instruction, list);
                method.instructions.remove(instruction.getPrevious());
                method.instructions.remove(instruction);
            }
        }
    }
    
    public boolean safe(final MethodNode method) {
        if (method.name.equals("<clinit>")) {
            return false;
        }
        if (method.name.equals("<init>")) {
            return false;
        }
        if (method.instructions.size() == 0) {
            return false;
        }
        if (method.instructions.size() >= 13107) {
            return false;
        }
        if (method.tryCatchBlocks.size() > 0) {
            return false;
        }
        if ((method.access & 0x1000) != 0x0) {
            return false;
        }
        for (final AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof LdcInsnNode) {
                final LdcInsnNode ldc = (LdcInsnNode)instruction;
                if (ldc.cst instanceof Handle || ldc.cst instanceof Type) {
                    return false;
                }
                continue;
            }
            else {
                if (instruction instanceof InvokeDynamicInsnNode) {
                    return false;
                }
                continue;
            }
        }
        return true;
    }
}
