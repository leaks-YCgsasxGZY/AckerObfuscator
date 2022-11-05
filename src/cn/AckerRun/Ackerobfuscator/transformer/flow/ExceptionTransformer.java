package cn.AckerRun.Ackerobfuscator.transformer.flow;

import cn.AckerRun.Ackerobfuscator.transformer.*;
import cn.AckerRun.Ackerobfuscator.*;
import cn.AckerRun.Ackerobfuscator.utils.pair.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.apache.commons.lang3.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.analysis.Frame;

public class ExceptionTransformer extends Transformer
{
    public static String handlerName;
    
    public ExceptionTransformer(final AckerObfuscator obf) {
        super(obf);
    }
    
    @Override
    public String getSection() {
        return "flow.exceptions";
    }
    
    private void visitMethod(final ClassWrapper classNode, final MethodNode method) {
        final ClassMethodNode cmn = new ClassMethodNode(classNode, method);
        if (this.target == null || this.target.equals(cmn)) {
            int amt = 0;
            for (final AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof VarInsnNode && instruction.getOpcode() >= 54 && instruction.getOpcode() <= 58) {
                    ++amt;
                }
            }
            if (AsmUtils.codeSize(method) + amt * 15 >= 65535) {
                return;
            }
            final Analyzer<BasicValue> analyzer = new Analyzer<BasicValue>(new BasicInterpreter());
            Frame<BasicValue>[] frames;
            try {
                frames = analyzer.analyzeAndComputeMaxs(classNode.name, method);
            }
            catch (Exception ex) {
                this.error("Failed to analyze method %s ", ex.getMessage());
                return;
            }
            for (final AbstractInsnNode instruction2 : method.instructions) {
                if (instruction2 instanceof VarInsnNode) {
                    final VarInsnNode var = (VarInsnNode)instruction2;
                    if (instruction2.getOpcode() < 54 || instruction2.getOpcode() > 58) {
                        continue;
                    }
                    final Frame<BasicValue> frame = frames[instruction2.index];
                    if (frame == null) {
                        continue;
                    }
                    if (frame.getStackSize() > 1) {
                        continue;
                    }
                    final BasicValue value = frame.getStack(frame.getStackSize() - 1);
                    final Type type = value.getType();
                    if (value == BasicValue.UNINITIALIZED_VALUE || type.getInternalName().equals("null")) {
                        continue;
                    }
                    if (type.getInternalName().equals("java/lang/Object")) {
                        continue;
                    }
                    final LabelNode start = new LabelNode();
                    final LabelNode end = new LabelNode();
                    final LabelNode handler = new LabelNode();
                    final LabelNode finish = new LabelNode();
                    final TryCatchBlockNode tryCatch = new TryCatchBlockNode(start, end, handler, ExceptionTransformer.handlerName);
                    final InsnList list = new InsnList();
                    list.add(start);
                    AsmUtils.boxPrimitive(type.getDescriptor(), list);
                    list.add(new TypeInsnNode(187, ExceptionTransformer.handlerName));
                    list.add(new InsnNode(95));
                    list.add(new InsnNode(92));
                    list.add(new InsnNode(87));
                    list.add(new InsnNode(95));
                    list.add(new MethodInsnNode(183, ExceptionTransformer.handlerName, "<init>", "(Ljava/lang/Object;)V", false));
                    list.add(new InsnNode(191));
                    list.add(new JumpInsnNode(167, start));
                    list.add(handler);
                    list.add(new FieldInsnNode(180, ExceptionTransformer.handlerName, "o", "Ljava/lang/Object;"));
                    AsmUtils.unboxPrimitive(type.getDescriptor(), list);
                    list.add(end);
                    list.add(new JumpInsnNode(167, finish));
                    list.add(finish);
                    list.add(instruction2.clone(null));
                    method.instructions.insert(instruction2, list);
                    method.instructions.remove(instruction2);
                    method.tryCatchBlocks.add(tryCatch);
                }
            }
        }
    }
    
    @Override
    protected void visit(final ClassWrapper classNode) {
        while (ExceptionTransformer.handlerName == null) {
            for (final ClassWrapper cn : new ArrayList<ClassWrapper>(this.obf.getClasses())) {
                if (this.nextBoolean(5)) {
                    ExceptionTransformer.handlerName = AsmUtils.parentName(cn.name) + RandomStringUtils.randomAlphabetic(3);
                    if (this.obf.assureLoaded(ExceptionTransformer.handlerName) != null) {
                        ExceptionTransformer.handlerName = null;
                    }
                    else {
                        final ClassWrapper handlerClass = new ClassWrapper(false);
                        handlerClass.visit(52, 1, ExceptionTransformer.handlerName, null, "java/lang/Throwable", null);
                        final FieldVisitor fv = handlerClass.visitField(1, "o", "Ljava/lang/Object;", null, null);
                        fv.visitEnd();
                        final MethodVisitor mv = handlerClass.visitMethod(1, "<init>", "(Ljava/lang/Object;)V", null, null);
                        mv.visitCode();
                        mv.visitVarInsn(25, 1);
                        mv.visitVarInsn(25, 0);
                        mv.visitInsn(89);
                        mv.visitMethodInsn(183, "java/lang/Throwable", "<init>", "()V", false);
                        mv.visitInsn(95);
                        mv.visitFieldInsn(181, ExceptionTransformer.handlerName, "o", "Ljava/lang/Object;");
                        mv.visitInsn(177);
                        mv.visitMaxs(2, 2);
                        mv.visitEnd();
                        this.obf.getClasses().add(handlerClass);
                    }
                }
            }
        }
        for (final MethodNode method : classNode.methods) {
            this.visitMethod(classNode, method);
        }
    }
}
