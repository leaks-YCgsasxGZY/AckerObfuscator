package cn.AckerRun.Ackerobfuscator.utils.asm;

import org.objectweb.asm.commons.*;
import java.io.*;
import cn.AckerRun.Ackerobfuscator.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.*;
import java.util.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.*;

public class AsmUtils implements Opcodes
{
    public static final int MAX_INSTRUCTIONS = 65535;
    private static final Printer printer;
    private static final TraceMethodVisitor methodPrinter;
    
    public static InsnList println(final String message) {
        final InsnList list = new InsnList();
        list.add(new FieldInsnNode(178, "java/lang/System", "out", "Ljava/io/PrintStream;"));
        list.add(new LdcInsnNode(message));
        list.add(new MethodInsnNode(182, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
        return list;
    }
    
    public static boolean isPushInt(final AbstractInsnNode insn) {
        final int op = insn.getOpcode();
        return (op >= 2 && op <= 8) || op == 16 || op == 17 || (op == 18 && ((LdcInsnNode)insn).cst instanceof Integer);
    }
    
    public static int getPushedInt(final AbstractInsnNode insn) {
        final int op = insn.getOpcode();
        if (op >= 2 && op <= 8) {
            return op - 3;
        }
        if (op == 16 || op == 17) {
            return ((IntInsnNode)insn).operand;
        }
        if (op == 18) {
            final Object cst = ((LdcInsnNode)insn).cst;
            if (cst instanceof Integer) {
                return (int)cst;
            }
        }
        throw new IllegalArgumentException("insn is not a push int instruction");
    }
    
    public static MethodNode getClinit(final ClassWrapper classNode) {
        for (final MethodNode method : classNode.methods) {
            if (method.name.equals("<clinit>")) {
                return method;
            }
        }
        final MethodNode clinit = new MethodNode(8, "<clinit>", "()V", null, null);
        (clinit.instructions = new InsnList()).add(new InsnNode(177));
        classNode.methods.add(clinit);
        return clinit;
    }
    
    public static AbstractInsnNode pushInt(final int value) {
        if (value >= -1 && value <= 5) {
            return new InsnNode(3 + value);
        }
        if (value >= -128 && value <= 127) {
            return new IntInsnNode(16, value);
        }
        if (value >= -32768 && value <= 32767) {
            return new IntInsnNode(17, value);
        }
        return new LdcInsnNode((Object)value);
    }
    
    public static boolean isPushLong(final AbstractInsnNode insn) {
        final int op = insn.getOpcode();
        return op == 9 || op == 10 || (op == 18 && ((LdcInsnNode)insn).cst instanceof Long);
    }
    
    public static long getPushedLong(final AbstractInsnNode insn) {
        final int op = insn.getOpcode();
        if (op == 9) {
            return 0L;
        }
        if (op == 10) {
            return 1L;
        }
        if (op == 18) {
            final Object cst = ((LdcInsnNode)insn).cst;
            if (cst instanceof Long) {
                return (long)cst;
            }
        }
        throw new IllegalArgumentException("insn is not a push long instruction");
    }
    
    public static AbstractInsnNode pushLong(final long value) {
        if (value == 0L) {
            return new InsnNode(9);
        }
        if (value == 1L) {
            return new InsnNode(10);
        }
        return new LdcInsnNode(value);
    }
    
    public static int codeSize(final MethodNode methodNode) {
        final CodeSizeEvaluator evaluator = new CodeSizeEvaluator(null);
        methodNode.accept(evaluator);
        return evaluator.getMaxSize();
    }
    
    public static void unboxPrimitive(final String desc, final InsnList list) {
        switch (desc) {
            case "I": {
                list.add(new TypeInsnNode(192, "java/lang/Integer"));
                list.add(new MethodInsnNode(182, "java/lang/Integer", "intValue", "()I", false));
                break;
            }
            case "Z": {
                list.add(new TypeInsnNode(192, "java/lang/Boolean"));
                list.add(new MethodInsnNode(182, "java/lang/Boolean", "booleanValue", "()Z", false));
                break;
            }
            case "B": {
                list.add(new TypeInsnNode(192, "java/lang/Byte"));
                list.add(new MethodInsnNode(182, "java/lang/Byte", "byteValue", "()B", false));
                break;
            }
            case "C": {
                list.add(new TypeInsnNode(192, "java/lang/Character"));
                list.add(new MethodInsnNode(182, "java/lang/Character", "charValue", "()C", false));
                break;
            }
            case "S": {
                list.add(new TypeInsnNode(192, "java/lang/Short"));
                list.add(new MethodInsnNode(182, "java/lang/Short", "shortValue", "()S", false));
                break;
            }
            case "J": {
                list.add(new TypeInsnNode(192, "java/lang/Long"));
                list.add(new MethodInsnNode(182, "java/lang/Long", "longValue", "()J", false));
                break;
            }
            case "F": {
                list.add(new TypeInsnNode(192, "java/lang/Float"));
                list.add(new MethodInsnNode(182, "java/lang/Float", "floatValue", "()F", false));
                break;
            }
            case "D": {
                list.add(new TypeInsnNode(192, "java/lang/Double"));
                list.add(new MethodInsnNode(182, "java/lang/Double", "doubleValue", "()D", false));
                break;
            }
            default: {
                if (!desc.equals("Lnull;") && !desc.equals("Ljava/lang/Object;")) {
                    list.add(new TypeInsnNode(192, (desc.startsWith("L") && desc.endsWith(";")) ? desc.substring(1, desc.length() - 1) : desc));
                    break;
                }
                break;
            }
        }
    }
    
    public static void boxPrimitive(final String desc, final InsnList list) {
        switch (desc) {
            case "I": {
                list.add(new MethodInsnNode(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
                break;
            }
            case "Z": {
                list.add(new MethodInsnNode(184, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false));
                break;
            }
            case "B": {
                list.add(new MethodInsnNode(184, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false));
                break;
            }
            case "C": {
                list.add(new MethodInsnNode(184, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false));
                break;
            }
            case "S": {
                list.add(new MethodInsnNode(184, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false));
                break;
            }
            case "J": {
                list.add(new MethodInsnNode(184, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false));
                break;
            }
            case "F": {
                list.add(new MethodInsnNode(184, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false));
                break;
            }
            case "D": {
                list.add(new MethodInsnNode(184, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false));
                break;
            }
            default: {
                if (!desc.equals("Lnull;") && !desc.equals("Ljava/lang/Object;")) {
                    list.add(new TypeInsnNode(192, (desc.startsWith("L") && desc.endsWith(";")) ? desc.substring(1, desc.length() - 1) : desc));
                    break;
                }
                break;
            }
        }
    }
    
    public static String print(final AbstractInsnNode insnNode) {
        if (insnNode == null) {
            return "null";
        }
        insnNode.accept(AsmUtils.methodPrinter);
        final StringWriter sw = new StringWriter();
        AsmUtils.printer.print(new PrintWriter(sw));
        AsmUtils.printer.getText().clear();
        return sw.toString().trim();
    }
    
    public static FieldNode findField(final AckerObfuscator obf, final String owner, final String name, final String desc) {
        final ClassWrapper classNode = obf.assureLoaded(owner);
        if (classNode == null) {
            return null;
        }
        return findField(classNode, name, desc);
    }
    
    public static FieldNode findField(final ClassWrapper classNode, final String name, final String desc) {
        for (final FieldNode field : classNode.fields) {
            if (field.name.equals(name) && (desc == null || field.desc.equals(desc))) {
                return field;
            }
        }
        return null;
    }
    
    public static MethodNode findMethod(final AckerObfuscator obf, final String owner, final String name, final String descriptor) {
        final ClassWrapper classNode = obf.assureLoaded(owner);
        if (classNode == null) {
            return null;
        }
        return findMethod(classNode, name, descriptor);
    }
    
    public static MethodNode findMethod(final ClassWrapper classNode, final String name, final String descriptor) {
        for (final MethodNode method : classNode.methods) {
            if (method.name.equals(name) && (descriptor == null || method.desc.equals(descriptor))) {
                return method;
            }
        }
        return null;
    }
    
    public static LabelNode[] getLabels(final MethodNode method) {
        final List<LabelNode> labels = new ArrayList<LabelNode>();
        for (final AbstractInsnNode insnNode : method.instructions.toArray()) {
            if (insnNode instanceof LabelNode) {
                labels.add((LabelNode)insnNode);
            }
        }
        return labels.toArray(new LabelNode[0]);
    }
    
    public static InsnList iterate(final InsnList instructions, final AbstractInsnNode start, final AbstractInsnNode end) {
        final InsnList list = new InsnList();
        boolean f = false;
        for (final AbstractInsnNode instruction : instructions) {
            if (!f && instruction == start) {
                f = true;
            }
            if (f) {
                list.add(instruction);
            }
            if (instruction == end) {
                break;
            }
        }
        return list;
    }
    
    public static ClassWrapper clone(final ClassWrapper classNode) {
        final ClassWrapper c = new ClassWrapper(classNode.modify);
        classNode.accept(c);
        return c;
    }
    
    public static void boxClass(final InsnList list, final Type type) {
        final String descriptor = type.getDescriptor();
        switch (descriptor) {
            case "I": {
                list.add(new FieldInsnNode(178, "java/lang/Integer", "TYPE", "Ljava/lang/Class;"));
                break;
            }
            case "Z": {
                list.add(new FieldInsnNode(178, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;"));
                break;
            }
            case "B": {
                list.add(new FieldInsnNode(178, "java/lang/Byte", "TYPE", "Ljava/lang/Class;"));
                break;
            }
            case "C": {
                list.add(new FieldInsnNode(178, "java/lang/Character", "TYPE", "Ljava/lang/Class;"));
                break;
            }
            case "S": {
                list.add(new FieldInsnNode(178, "java/lang/Short", "TYPE", "Ljava/lang/Class;"));
                break;
            }
            case "J": {
                list.add(new FieldInsnNode(178, "java/lang/Long", "TYPE", "Ljava/lang/Class;"));
                break;
            }
            case "F": {
                list.add(new FieldInsnNode(178, "java/lang/Float", "TYPE", "Ljava/lang/Class;"));
                break;
            }
            case "D": {
                list.add(new FieldInsnNode(178, "java/lang/Double", "TYPE", "Ljava/lang/Class;"));
                break;
            }
            case "V": {
                list.add(new FieldInsnNode(178, "java/lang/Void", "TYPE", "Ljava/lang/Class;"));
                break;
            }
            default: {
                list.add(new LdcInsnNode(type));
                break;
            }
        }
    }
    
    public static MethodNode createMethod(final int access, final String name, final String desc) {
        final MethodNode m = new MethodNode(access, name, desc, null, null);
        m.instructions = new InsnList();
        return m;
    }
    
    public static void boxReturn(final Type returnType, final InsnList list) {
        final Random r = new Random();
        switch (returnType.getOpcode(172)) {
            case 172: {
                list.add(pushInt(r.nextInt()));
                break;
            }
            case 173: {
                list.add(pushLong(r.nextLong()));
                break;
            }
            case 174: {
                list.add(new LdcInsnNode(r.nextFloat()));
                break;
            }
            case 175: {
                list.add(new LdcInsnNode(r.nextDouble()));
                break;
            }
            case 176: {
                list.add(new InsnNode(1));
                break;
            }
            case 177: {
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown return type: " + returnType);
            }
        }
        list.add(new InsnNode(returnType.getOpcode(172)));
    }
    
    public static String parentName(final String name) {
        if (name.contains("/")) {
            return name.substring(0, name.lastIndexOf("/") + 1);
        }
        return "";
    }
    
    public static MethodNode findMethodSuper(final ClassWrapper owner, final String name, final String desc) {
        for (ClassWrapper superWrapper = owner; superWrapper != null; superWrapper = AckerObfuscator.getInstance().assureLoaded(superWrapper.superName)) {
            final MethodNode m = findMethod(superWrapper, name, desc);
            if (m != null) {
                return m;
            }
            if (superWrapper.superName == null) {
                break;
            }
            if (superWrapper.superName.isEmpty()) {
                break;
            }
        }
        return null;
    }
    
    public static FieldNode findFieldSuper(final ClassWrapper ownerClass, final String name, final String desc) {
        for (ClassWrapper superWrapper = ownerClass; superWrapper != null; superWrapper = AckerObfuscator.getInstance().assureLoaded(superWrapper.superName)) {
            final FieldNode m = findField(superWrapper, name, desc);
            if (m != null) {
                return m;
            }
            if (superWrapper.superName == null) {
                break;
            }
            if (superWrapper.superName.isEmpty()) {
                break;
            }
        }
        return null;
    }
    
    public static void preverify(final ClassWrapper classNode, final MethodNode method) {
        final Analyzer<SourceValue> analyzer = new Analyzer<SourceValue>(new SourceInterpreter());
        try {
            analyzer.analyzeAndComputeMaxs(classNode.name, method);
        }
        catch (AnalyzerException e) {
            System.out.println("Failed to preverify method: " + classNode.name + "." + method.name + method.desc);
            e.printStackTrace();
        }
    }
    
    static {
        printer = new Textifier();
        methodPrinter = new TraceMethodVisitor(AsmUtils.printer);
    }
}
