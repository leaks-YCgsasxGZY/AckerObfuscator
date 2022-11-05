package cn.AckerRun.Ackerobfuscator.transformer.misc;

import cn.AckerRun.Ackerobfuscator.transformer.*;
import cn.AckerRun.Ackerobfuscator.*;
import org.apache.commons.lang3.*;
import org.objectweb.asm.*;
import java.io.*;
import java.util.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.tree.*;
import cn.AckerRun.Ackerobfuscator.utils.pair.*;

public class ChecksumTransformer extends Transformer
{
    private final List<String> targets;
    private List<String> holders;
    private final boolean reobfTarget;
    private final boolean randomHolders;
    
    public ChecksumTransformer(final AckerObfuscator obf) {
        super(obf);
        this.targets = this.config.getStringList("targets");
        this.reobfTarget = this.config.getBoolean("reobf", true);
        this.holders = this.config.getStringList("holders");
        this.randomHolders = this.config.getBoolean("randomHolders", false);
        if (this.randomHolders) {
            this.holders = new ArrayList<String>();
            for (final ClassWrapper classNode : obf.getClasses()) {
                if (this.targets.contains(classNode.name)) {
                    continue;
                }
                this.holders.add(classNode.name);
            }
        }
    }
    
    @Override
    public String getSection() {
        return "misc.checksum";
    }
    
    @Override
    protected void after() {
        try {
            if (this.targets.isEmpty() || this.holders.isEmpty()) {
                return;
            }
            if (!this.randomHolders) {
                for (final String target : this.targets) {
                    for (final String holder : this.holders) {
                        if (target.equals(holder)) {
                            this.error("Target and holder are the same: %s", target);
                        }
                    }
                }
            }
            final List<ClassWrapper> classNodes = new ArrayList<ClassWrapper>(this.obf.getClasses());
            Collections.shuffle(classNodes);
            boolean applied = false;
            for (final ClassWrapper classNode : classNodes) {
                if (this.targets.contains(classNode.name)) {
                    Collections.shuffle(this.holders);
                    final String holderName = this.holders.get(0);
                    final ContextClassWriter writer = new ContextClassWriter(2);
                    classNode.accept(writer);
                    final byte[] b = writer.toByteArray();
                    this.obf.addGeneratedClass(classNode.name, b);
                    final ClassWrapper holder2 = this.obf.assureLoaded(holderName);
                    if (holder2 == null) {
                        this.error("Holder class not found: %s", holderName);
                        break;
                    }
                    this.log("Applying checksum to %s inside of %s", classNode.name, holderName);
                    applied = true;
                    String name = this.config.getString("method-name");
                    if (name.equals("random")) {
                        name = RandomStringUtils.random(10);
                    }
                    final String desc = "()V";
                    final MethodNode checkMethod = new MethodNode(4106, name, desc, null, null);
                    checkMethod.visitCode();
                    final Label label2 = new Label();
                    final Label label3 = new Label();
                    if (this.config.getBoolean("reduced", false) || this.obf.isTransformerEnabled(PackerTransformer.class)) {
                        this.log("Reduced checksum check due to packer", new Object[0]);
                        checkMethod.visitLdcInsn(Type.getType("L" + holder2.name + ";"));
                        final String s = "/" + classNode.name + ".class";
                        checkMethod.visitLdcInsn(s);
                        checkMethod.visitMethodInsn(182, "java/lang/Class", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false);
                        checkMethod.visitJumpInsn(199, label3);
                        checkMethod.visitJumpInsn(167, label2);
                    }
                    else {
                        final int r1 = this.random.nextInt();
                        final int r2 = this.random.nextInt();
                        int real = r1;
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        final ByteArrayInputStream bais = new ByteArrayInputStream(b);
                        final byte[] byArray = new byte[4096];
                        while (true) {
                            final int n = bais.read(byArray, 0, byArray.length);
                            real ^= (r2 ^ baos.size());
                            if (n == -1) {
                                break;
                            }
                            baos.write(byArray, 0, n);
                        }
                        final int bStart = writer.offsetCPStart;
                        final int bEnd = writer.offsetMethodEnd;
                        final byte[] b2 = new byte[bEnd - bStart];
                        System.arraycopy(b, bStart, b2, 0, b2.length);
                        final int hash = Arrays.hashCode(b2) ^ real;
                        this.log("Hash: %d Start: %d End: %d", hash, bStart, bEnd);
                        checkMethod.visitLdcInsn(r1);
                        checkMethod.visitVarInsn(54, 5);
                        checkMethod.visitLdcInsn(Type.getType("L" + holder2.name + ";"));
                        final char[] s2 = ("/" + classNode.name + ".class").toCharArray();
                        checkMethod.visitTypeInsn(187, "java/lang/String");
                        checkMethod.visitInsn(89);
                        checkMethod.visitLdcInsn(s2.length);
                        checkMethod.visitIntInsn(188, 5);
                        for (int i = 0; i < s2.length; ++i) {
                            checkMethod.visitInsn(89);
                            checkMethod.instructions.add(AsmUtils.pushInt(i));
                            checkMethod.instructions.add(AsmUtils.pushInt(s2[i] ^ r1));
                            checkMethod.visitVarInsn(21, 5);
                            checkMethod.visitInsn(130);
                            checkMethod.visitInsn(146);
                            checkMethod.visitInsn(85);
                        }
                        checkMethod.visitMethodInsn(183, "java/lang/String", "<init>", "([C)V", false);
                        checkMethod.visitMethodInsn(182, "java/lang/Class", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false);
                        checkMethod.visitVarInsn(58, 1);
                        checkMethod.visitVarInsn(25, 1);
                        checkMethod.visitJumpInsn(198, label3);
                        checkMethod.visitTypeInsn(187, "java/io/ByteArrayOutputStream");
                        checkMethod.visitInsn(89);
                        checkMethod.visitMethodInsn(183, "java/io/ByteArrayOutputStream", "<init>", "()V", false);
                        checkMethod.visitVarInsn(58, 2);
                        checkMethod.visitIntInsn(17, 4096);
                        checkMethod.visitIntInsn(188, 8);
                        checkMethod.visitVarInsn(58, 3);
                        final Label label4 = new Label();
                        checkMethod.visitLabel(label4);
                        checkMethod.visitInsn(2);
                        checkMethod.visitVarInsn(25, 1);
                        checkMethod.visitVarInsn(25, 3);
                        checkMethod.visitInsn(3);
                        checkMethod.visitVarInsn(25, 3);
                        checkMethod.visitInsn(190);
                        checkMethod.visitMethodInsn(182, "java/io/InputStream", "read", "([BII)I", false);
                        checkMethod.visitVarInsn(21, 5);
                        checkMethod.visitLdcInsn(r2);
                        checkMethod.visitVarInsn(25, 2);
                        checkMethod.visitMethodInsn(182, "java/io/ByteArrayOutputStream", "size", "()I", false);
                        checkMethod.visitInsn(130);
                        checkMethod.visitInsn(130);
                        checkMethod.visitVarInsn(54, 5);
                        checkMethod.visitInsn(89);
                        checkMethod.visitVarInsn(54, 4);
                        final Label label5 = new Label();
                        checkMethod.visitJumpInsn(159, label5);
                        checkMethod.visitVarInsn(25, 2);
                        checkMethod.visitVarInsn(25, 3);
                        checkMethod.visitInsn(3);
                        checkMethod.visitVarInsn(21, 4);
                        checkMethod.visitMethodInsn(182, "java/io/ByteArrayOutputStream", "write", "([BII)V", false);
                        checkMethod.visitJumpInsn(167, label4);
                        checkMethod.visitLabel(label5);
                        checkMethod.visitVarInsn(25, 2);
                        checkMethod.visitMethodInsn(182, "java/io/ByteArrayOutputStream", "toByteArray", "()[B", false);
                        checkMethod.visitLdcInsn(bEnd - bStart);
                        checkMethod.visitIntInsn(188, 8);
                        checkMethod.visitVarInsn(58, 6);
                        checkMethod.visitLdcInsn(bStart);
                        checkMethod.visitVarInsn(25, 6);
                        checkMethod.visitInsn(3);
                        checkMethod.visitVarInsn(25, 6);
                        checkMethod.visitInsn(190);
                        checkMethod.visitMethodInsn(184, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
                        checkMethod.visitVarInsn(25, 6);
                        checkMethod.visitMethodInsn(184, "java/util/Arrays", "hashCode", "([B)I", false);
                        checkMethod.visitVarInsn(21, 5);
                        checkMethod.visitLdcInsn(hash);
                        checkMethod.visitInsn(130);
                        checkMethod.visitJumpInsn(159, label2);
                    }
                    checkMethod.visitLabel(label3);
                    checkMethod.visitMethodInsn(184, "sun/misc/Launcher", "getLauncher", "()Lsun/misc/Launcher;", false);
                    checkMethod.visitMethodInsn(182, "sun/misc/Launcher", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
                    checkMethod.visitInsn(89);
                    checkMethod.visitVarInsn(58, 7);
                    checkMethod.visitMethodInsn(182, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
                    checkMethod.visitMethodInsn(182, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;", false);
                    checkMethod.visitMethodInsn(182, "java/lang/Class", "getDeclaredFields", "()[Ljava/lang/reflect/Field;", false);
                    checkMethod.visitInsn(3);
                    checkMethod.visitInsn(50);
                    checkMethod.visitVarInsn(58, 8);
                    checkMethod.visitVarInsn(25, 8);
                    checkMethod.visitInsn(4);
                    checkMethod.visitMethodInsn(182, "java/lang/reflect/Field", "setAccessible", "(Z)V", false);
                    checkMethod.visitVarInsn(25, 8);
                    checkMethod.visitVarInsn(25, 7);
                    checkMethod.visitTypeInsn(187, "sun/misc/URLClassPath");
                    checkMethod.visitInsn(89);
                    checkMethod.visitInsn(3);
                    checkMethod.visitTypeInsn(189, "java/net/URL");
                    checkMethod.visitInsn(1);
                    checkMethod.visitMethodInsn(183, "sun/misc/URLClassPath", "<init>", "([Ljava/net/URL;Ljava/security/AccessControlContext;)V", false);
                    checkMethod.visitMethodInsn(182, "java/lang/reflect/Field", "set", "(Ljava/lang/Object;Ljava/lang/Object;)V", false);
                    checkMethod.visitLabel(label2);
                    checkMethod.visitInsn(177);
                    checkMethod.visitEnd();
                    holder2.methods.add(checkMethod);
                    final Analyzer<?> analyzer = new Analyzer<>(new BasicInterpreter());
                    analyzer.analyzeAndComputeMaxs(holder2.name, checkMethod);
                    final MethodNode clinit = AsmUtils.getClinit(holder2);
                    clinit.instructions.insertBefore(clinit.instructions.getFirst(), new MethodInsnNode(184, holder2.name, name, desc));
                    if (this.reobfTarget) {
                        for (final Transformer transformer : this.obf.getTransformers()) {
                            if (!transformer.canBeIterated) {
                                continue;
                            }
                            transformer.target = new ClassMethodNode(holder2, checkMethod);
                            transformer.run(holder2);
                            transformer.target = null;
                        }
                    }
                    break;
                }
            }
            if (!applied) {
                this.error("Could not find any targets and holders", new Object[0]);
            }
        }
        catch (Throwable $ex) {
            $ex.printStackTrace();
        }
    }
}
