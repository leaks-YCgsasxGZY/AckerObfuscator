package cn.AckerRun.Ackerobfuscator.transformer.misc;

import cn.AckerRun.Ackerobfuscator.transformer.*;
import cn.AckerRun.Ackerobfuscator.*;
import cn.AckerRun.Ackerobfuscator.utils.pair.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.*;

public class VariableTransformer extends Transformer
{
    public VariableTransformer(final AckerObfuscator obf) {
        super(obf);
    }
    
    @Override
    public String getSection() {
        return "misc.vars";
    }
    
    @Override
    protected void visit(final ClassWrapper classNode) {
        try {
            for (final MethodNode method : classNode.methods) {
                final ClassMethodNode cmn = new ClassMethodNode(classNode, method);
                if (this.target == null || this.target.equals(cmn)) {
                    if (method.instructions.size() == 0) {
                        continue;
                    }
                    if (AsmUtils.codeSize(method) * 2 >= 50000) {
                        continue;
                    }
                    final Analyzer<BasicValue> analyzer = new Analyzer<BasicValue>(new BasicInterpreter());
                    Frame<BasicValue>[] frames;
                    try {
                        frames = analyzer.analyzeAndComputeMaxs(classNode.name, method);
                    }
                    catch (Exception ex) {
                        this.error("Failed to analyze method %s ", ex.getMessage());
                        continue;
                    }
                    int nonLocals = 0;
                    if (!Modifier.isStatic(method.access)) {
                        ++nonLocals;
                    }
                    nonLocals += Arrays.stream(Type.getArgumentTypes(method.desc)).mapToInt(Type::getSize).sum();
                    final int amt = method.maxLocals - nonLocals;
                    final int arrayVar = method.maxLocals;
                    final Map<Integer, Integer> varMap = new HashMap<Integer, Integer>();
                    final LinkedList<Integer> failedVars = new LinkedList<Integer>();
                    final LinkedList<Integer> potential = new LinkedList<Integer>();
                    for (int i = 0; i < amt; ++i) {
                        potential.add(i);
                    }
                    Collections.shuffle(potential);
                    for (final AbstractInsnNode instruction : method.instructions) {
                        if (instruction instanceof VarInsnNode) {
                            final VarInsnNode var = (VarInsnNode)instruction;
                            if (failedVars.contains(var.var)) {
                                continue;
                            }
                            if (var.var < nonLocals) {
                                continue;
                            }
                            final boolean load = var.getOpcode() >= 21 && var.getOpcode() <= 25;
                            final Frame<BasicValue> frame = frames[instruction.index + (load ? 1 : 0)];
                            if (frame == null) {
                                failedVars.add(var.var);
                            }
                            else {
                                final BasicValue value = frame.getStack(frame.getStackSize() - 1);
                                final Type type = value.getType();
                                if (value == BasicValue.UNINITIALIZED_VALUE || type.getInternalName().equals("null") || type.getInternalName().equals("java/lang/Object")) {
                                    failedVars.add(var.var);
                                }
                                else {
                                    if (!varMap.containsKey(var.var)) {
                                        varMap.put(var.var, potential.pop());
                                    }
                                    final InsnList list = new InsnList();
                                    if (load) {
                                        list.add(new VarInsnNode(25, arrayVar));
                                        list.add(AsmUtils.pushInt(varMap.get(var.var)));
                                        list.add(new InsnNode(50));
                                        AsmUtils.unboxPrimitive(type.getDescriptor(), list);
                                    }
                                    else {
                                        AsmUtils.boxPrimitive(type.getDescriptor(), list);
                                        list.add(new VarInsnNode(25, arrayVar));
                                        list.add(new InsnNode(95));
                                        list.add(AsmUtils.pushInt(varMap.get(var.var)));
                                        list.add(new InsnNode(95));
                                        list.add(new InsnNode(83));
                                    }
                                    method.instructions.insertBefore(instruction, list);
                                    method.instructions.remove(instruction);
                                }
                            }
                        }
                    }
                    for (final AbstractInsnNode instruction : method.instructions) {
                        if (instruction instanceof IincInsnNode) {
                            final IincInsnNode inc = (IincInsnNode)instruction;
                            if (!varMap.containsKey(inc.var)) {
                                continue;
                            }
                            final int var2 = varMap.get(inc.var);
                            final InsnList list2 = new InsnList();
                            list2.add(new VarInsnNode(25, arrayVar));
                            list2.add(AsmUtils.pushInt(var2));
                            list2.add(new InsnNode(92));
                            list2.add(new InsnNode(50));
                            AsmUtils.unboxPrimitive("I", list2);
                            list2.add(AsmUtils.pushInt(inc.incr));
                            list2.add(new InsnNode(96));
                            AsmUtils.boxPrimitive("I", list2);
                            list2.add(new InsnNode(83));
                            method.instructions.insertBefore(instruction, list2);
                            method.instructions.remove(instruction);
                        }
                    }
                    if (amt <= 0) {
                        continue;
                    }
                    final MethodNode methodNode = method;
                    ++methodNode.maxLocals;
                    final InsnList start = new InsnList();
                    start.add(AsmUtils.pushInt(amt));
                    start.add(new TypeInsnNode(189, "java/lang/Object"));
                    start.add(new VarInsnNode(58, arrayVar));
                    method.instructions.insertBefore(method.instructions.getFirst(), start);
                }
            }
        }
        catch (Throwable $ex) {
            throw $ex;
        }
    }
}
