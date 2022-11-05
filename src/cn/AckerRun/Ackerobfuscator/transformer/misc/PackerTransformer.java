package cn.AckerRun.Ackerobfuscator.transformer.misc;

import cn.AckerRun.Ackerobfuscator.transformer.*;
import cn.AckerRun.Ackerobfuscator.*;
import java.lang.reflect.*;
import java.util.jar.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import org.objectweb.asm.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import java.io.*;

public class PackerTransformer extends Transformer
{
    public PackerTransformer(final AckerObfuscator obf) {
        super(obf);
    }
    
    public static boolean isClass(final byte[] bytes) {
        if (bytes == null) {
            return false;
        }
        if (bytes.length < 6) {
            return false;
        }
        final boolean res = bytes[0] == -54 && bytes[1] == -2 && bytes[2] == -70 && bytes[3] == -66;
        return res;
    }
    
    @Override
    protected void after() {
        final int key = this.random.nextInt();
        ClassWrapper mainClass = null;
        if (this.config.getBoolean("forge")) {
            AnnotationNode annotation = null;
            final LinkedList<MethodNode> events = new LinkedList<MethodNode>();
            for (final ClassWrapper classNode : this.obf.getClasses()) {
                if (classNode.visibleAnnotations != null) {
                    for (final AnnotationNode visibleAnnotation : classNode.visibleAnnotations) {
                        if (visibleAnnotation.desc.contains("net/minecraftforge/fml/common/Mod")) {
                            annotation = visibleAnnotation;
                            final ClassWrapper classWrapper = classNode;
                            classWrapper.access |= 0x1;
                            final ClassWrapper classWrapper2 = classNode;
                            classWrapper2.access &= 0xFFFFFFEF;
                            mainClass = classNode;
                            break;
                        }
                    }
                }
                for (final MethodNode method : classNode.methods) {
                    if (method.visibleAnnotations != null) {
                        for (final AnnotationNode visibleAnnotation2 : new ArrayList<AnnotationNode>(method.visibleAnnotations)) {
                            if (visibleAnnotation2.desc.equals("Lnet/minecraftforge/fml/common/Mod$EventHandler;")) {
                                events.add(method);
                                method.visibleAnnotations.remove(visibleAnnotation2);
                            }
                        }
                    }
                }
            }
            if (annotation == null) {
                this.error("Could not find Mod annotation", new Object[0]);
                return;
            }
            final ClassWrapper cw = new ClassWrapper(true);
            String superName = this.config.getString("class-super-name");
            if (superName.equals("random")) {
                final LinkedList<ClassWrapper> compatible = new LinkedList<ClassWrapper>();
                for (final ClassWrapper lib : this.obf.getLibs()) {
                    if (!lib.name.startsWith("java/lang")) {
                        continue;
                    }
                    if (AsmUtils.findMethod(lib, "<clinit>", "()V") != null) {
                        continue;
                    }
                    if (!Modifier.isPublic(lib.access)) {
                        continue;
                    }
                    for (final MethodNode method2 : lib.methods) {
                        if (method2.name.equals("<init>") && Modifier.isPublic(method2.access) && method2.desc.equals("()V")) {
                            compatible.add(lib);
                        }
                    }
                }
                Collections.shuffle(compatible, this.random);
                superName = compatible.getFirst().name;
                this.log("Using super class %s", superName);
            }
            cw.visit(52, 33, this.config.getString("class-name"), null, superName, null);
            (cw.visibleAnnotations = new ArrayList<AnnotationNode>(1)).add(annotation);
            final FieldVisitor fv = cw.visitField(9, "instance", "L" + mainClass.name + ";", null, null);
            fv.visitEnd();
            this.insertForgeStub(cw, mainClass, key);
            final MethodVisitor mv = cw.visitMethod(1, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(25, 0);
            mv.visitMethodInsn(183, superName, "<init>", "()V");
            mv.visitInsn(177);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
            cw.visitEnd();
            final byte[] packed = this.pack(key);
            this.obf.getResources().clear();
            this.obf.addResource(this.config.getString("resource-name"), packed);
            if (this.config.getBoolean("reobf")) {
                for (final Transformer transformer : this.obf.getTransformers()) {
                    if (!transformer.canBeIterated) {
                        continue;
                    }
                    transformer.run(cw);
                }
            }
            for (final MethodNode event : events) {
                final MethodNode mn = new MethodNode(event.access, event.name, event.desc, null, null);
                mn.visitAnnotation("Lnet/minecraftforge/fml/common/Mod$EventHandler;", true).visitEnd();
                mn.visitCode();
                final Type[] args = Type.getArgumentTypes(event.desc);
                if (!Modifier.isStatic(event.access)) {
                    mn.visitFieldInsn(178, cw.name, "instance", "L" + mainClass.name + ";");
                    mn.maxLocals = 1;
                }
                for (final Type arg : args) {
                    mn.visitVarInsn(arg.getOpcode(21), mn.maxLocals);
                    final MethodNode methodNode = mn;
                    methodNode.maxLocals += arg.getSize();
                }
                mn.visitMethodInsn(Modifier.isStatic(event.access) ? 184 : 182, mainClass.name, event.name, event.desc);
                mn.visitInsn(Type.getReturnType(event.desc).getOpcode(172));
                mn.visitEnd();
                mn.visitMaxs(mn.maxLocals, mn.maxLocals + 1);
                cw.methods.add(mn);
            }
            this.obf.addClass(cw);
        }
        else {
            final Manifest manifest = this.obf.getManifest();
            final String mainName = manifest.getMainAttributes().getValue("Main-Class").replace('.', '/');
            mainClass = this.obf.assureLoaded(mainName);
            if (mainClass == null) {
                this.error("Main class %s not found", mainName);
            }
        }
    }
    
    private void insertForgeStub(final ClassWrapper cw, final ClassWrapper mainClass, final int randomKey) {
        final MethodNode mv = (MethodNode)cw.visitMethod(8, "<clinit>", "()V", null, null);
        mv.visitCode();
        final Label label0 = new Label();
        final Label label2 = new Label();
        final Label label3 = new Label();
        mv.visitTryCatchBlock(label0, label2, label3, "java/lang/Throwable");
        mv.visitLabel(label0);
        mv.visitTypeInsn(187, "java/io/ByteArrayInputStream");
        mv.visitInsn(89);
        mv.visitLdcInsn(Type.getType("L" + cw.name + ";"));
        mv.visitLdcInsn("/" + this.config.getString("resource-name"));
        mv.visitMethodInsn(182, "java/lang/Class", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false);
        mv.visitMethodInsn(184, "org/apache/commons/io/IOUtils", "toByteArray", "(Ljava/io/InputStream;)[B", false);
        mv.visitMethodInsn(183, "java/io/ByteArrayInputStream", "<init>", "([B)V", false);
        mv.visitVarInsn(58, 3);
        mv.visitTypeInsn(187, "java/io/DataInputStream");
        mv.visitInsn(89);
        mv.visitVarInsn(25, 3);
        mv.visitMethodInsn(183, "java/io/DataInputStream", "<init>", "(Ljava/io/InputStream;)V", false);
        mv.visitVarInsn(58, 4);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readInt", "()I", false);
        mv.visitInsn(87);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readInt", "()I", false);
        mv.visitInsn(87);
        final Label label4 = new Label();
        mv.visitLabel(label4);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "available", "()I", false);
        final Label label5 = new Label();
        mv.visitJumpInsn(158, label5);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readFloat", "()F", false);
        mv.visitLdcInsn(new Float("4.0"));
        mv.visitInsn(106);
        mv.visitInsn(139);
        mv.visitVarInsn(54, 5);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readUTF", "()Ljava/lang/String;", false);
        mv.visitVarInsn(58, 6);
        mv.visitVarInsn(21, 5);
        mv.visitIntInsn(188, 5);
        mv.visitVarInsn(58, 7);
        mv.visitInsn(3);
        mv.visitVarInsn(54, 8);
        final Label label6 = new Label();
        mv.visitLabel(label6);
        mv.visitVarInsn(21, 8);
        mv.visitVarInsn(21, 5);
        final Label label7 = new Label();
        mv.visitJumpInsn(162, label7);
        mv.visitVarInsn(25, 7);
        mv.visitVarInsn(21, 8);
        mv.visitVarInsn(25, 6);
        mv.visitVarInsn(21, 8);
        mv.visitMethodInsn(182, "java/lang/String", "charAt", "(I)C", false);
        mv.instructions.add(AsmUtils.pushInt(randomKey));
        mv.visitInsn(130);
        mv.visitVarInsn(21, 5);
        mv.visitInsn(130);
        mv.visitInsn(146);
        mv.visitInsn(85);
        mv.visitVarInsn(25, 7);
        mv.visitVarInsn(21, 8);
        mv.visitInsn(92);
        mv.visitInsn(52);
        mv.visitInsn(5);
        mv.visitInsn(122);
        mv.visitInsn(146);
        mv.visitInsn(85);
        mv.visitIincInsn(8, 1);
        mv.visitJumpInsn(167, label6);
        mv.visitLabel(label7);
        mv.visitTypeInsn(187, "java/lang/String");
        mv.visitInsn(89);
        mv.visitVarInsn(25, 7);
        mv.visitMethodInsn(183, "java/lang/String", "<init>", "([C)V", false);
        mv.visitVarInsn(58, 6);
        mv.visitIntInsn(16, 16);
        mv.visitIntInsn(188, 8);
        mv.visitVarInsn(58, 8);
        mv.visitVarInsn(25, 4);
        mv.visitVarInsn(25, 8);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readFully", "([B)V", false);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readInt", "()I", false);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readInt", "()I", false);
        mv.visitInsn(130);
        mv.visitIntInsn(188, 8);
        mv.visitVarInsn(58, 9);
        mv.visitVarInsn(25, 4);
        mv.visitVarInsn(25, 9);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readFully", "([B)V", false);
        mv.visitLdcInsn("AES/ECB/PKCS5Padding");
        mv.visitMethodInsn(184, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;", false);
        mv.visitVarInsn(58, 10);
        mv.visitTypeInsn(187, "javax/crypto/spec/SecretKeySpec");
        mv.visitInsn(89);
        mv.visitVarInsn(25, 8);
        mv.visitLdcInsn("AES");
        mv.visitMethodInsn(183, "javax/crypto/spec/SecretKeySpec", "<init>", "([BLjava/lang/String;)V", false);
        mv.visitVarInsn(58, 11);
        mv.visitVarInsn(25, 10);
        mv.visitInsn(5);
        mv.visitVarInsn(25, 11);
        mv.visitMethodInsn(182, "javax/crypto/Cipher", "init", "(ILjava/security/Key;)V", false);
        mv.visitVarInsn(25, 10);
        mv.visitVarInsn(25, 9);
        mv.visitMethodInsn(182, "javax/crypto/Cipher", "doFinal", "([B)[B", false);
        mv.visitVarInsn(58, 9);
        mv.visitTypeInsn(187, "java/lang/Object");
        mv.visitInsn(89);
        mv.visitMethodInsn(183, "java/lang/Object", "<init>", "()V", false);
        mv.visitVarInsn(58, 12);
        mv.visitMethodInsn(184, "net/minecraftforge/fml/common/Loader", "instance", "()Lnet/minecraftforge/fml/common/Loader;", false);
        mv.visitMethodInsn(182, "net/minecraftforge/fml/common/Loader", "getModClassLoader", "()Lnet/minecraftforge/fml/common/ModClassLoader;", false);
        mv.visitVarInsn(58, 12);
        mv.visitLdcInsn(Type.getType("Lnet/minecraftforge/fml/common/ModClassLoader;"));
        mv.visitVarInsn(25, 12);
        mv.visitTypeInsn(192, "net/minecraftforge/fml/common/ModClassLoader");
        mv.visitInsn(4);
        mv.visitTypeInsn(189, "java/lang/String");
        mv.visitInsn(89);
        mv.visitInsn(3);
        mv.visitLdcInsn("mainClassLoader");
        mv.visitInsn(83);
        mv.visitMethodInsn(184, "net/minecraftforge/fml/relauncher/ReflectionHelper", "getPrivateValue", "(Ljava/lang/Class;Ljava/lang/Object;[Ljava/lang/String;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(192, "net/minecraft/launchwrapper/LaunchClassLoader");
        mv.visitVarInsn(58, 12);
        mv.visitLdcInsn(Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
        mv.visitVarInsn(25, 12);
        mv.visitTypeInsn(192, "net/minecraft/launchwrapper/LaunchClassLoader");
        mv.visitInsn(4);
        mv.visitTypeInsn(189, "java/lang/String");
        mv.visitInsn(89);
        mv.visitInsn(3);
        mv.visitLdcInsn("resourceCache");
        mv.visitInsn(83);
        mv.visitMethodInsn(184, "net/minecraftforge/fml/relauncher/ReflectionHelper", "getPrivateValue", "(Ljava/lang/Class;Ljava/lang/Object;[Ljava/lang/String;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(192, "java/util/concurrent/ConcurrentHashMap");
        mv.visitInsn(89);
        mv.visitVarInsn(25, 6);
        mv.visitVarInsn(25, 9);
        mv.visitMethodInsn(182, "java/util/concurrent/ConcurrentHashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitInsn(87);
        mv.visitLdcInsn(Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
        mv.visitInsn(95);
        mv.visitVarInsn(25, 12);
        mv.visitTypeInsn(192, "net/minecraft/launchwrapper/LaunchClassLoader");
        mv.visitInsn(95);
        mv.visitInsn(4);
        mv.visitTypeInsn(189, "java/lang/String");
        mv.visitInsn(89);
        mv.visitInsn(3);
        mv.visitLdcInsn("resourceCache");
        mv.visitInsn(83);
        mv.visitMethodInsn(184, "net/minecraftforge/fml/relauncher/ReflectionHelper", "setPrivateValue", "(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/String;)V", false);
        mv.visitJumpInsn(167, label4);
        mv.visitLabel(label5);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "close", "()V", false);
        mv.visitVarInsn(25, 3);
        mv.visitMethodInsn(182, "java/io/ByteArrayInputStream", "close", "()V", false);
        mv.visitLabel(label2);
        final Label label8 = new Label();
        mv.visitJumpInsn(167, label8);
        mv.visitLabel(label3);
        mv.visitVarInsn(58, 3);
        mv.visitVarInsn(25, 3);
        mv.visitInsn(191);
        mv.visitLabel(label8);
        mv.visitTypeInsn(187, mainClass.name);
        mv.visitInsn(89);
        mv.visitMethodInsn(183, mainClass.name, "<init>", "()V");
        mv.visitFieldInsn(179, cw.name, "instance", "L" + mainClass.name + ";");
        mv.visitInsn(177);
        mv.visitMaxs(4, 13);
        mv.visitEnd();
    }
    
    private void insertNormalStub(final ClassWrapper cw, final ClassWrapper mainClass, final int randomKey) {
        final MethodNode mv = (MethodNode)cw.visitMethod(8, "<clinit>", "()V", null, null);
        mv.visitCode();
        final Label label0 = new Label();
        final Label label2 = new Label();
        final Label label3 = new Label();
        mv.visitTryCatchBlock(label0, label2, label3, "java/lang/Throwable");
        mv.visitLabel(label0);
        mv.visitTypeInsn(187, "java/io/ByteArrayInputStream");
        mv.visitInsn(89);
        mv.visitLdcInsn(Type.getType("L" + cw.name + ";"));
        mv.visitLdcInsn("/" + this.config.getString("resource-name"));
        mv.visitMethodInsn(182, "java/lang/Class", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false);
        mv.visitMethodInsn(184, "org/apache/commons/io/IOUtils", "toByteArray", "(Ljava/io/InputStream;)[B", false);
        mv.visitMethodInsn(183, "java/io/ByteArrayInputStream", "<init>", "([B)V", false);
        mv.visitVarInsn(58, 3);
        mv.visitTypeInsn(187, "java/io/DataInputStream");
        mv.visitInsn(89);
        mv.visitVarInsn(25, 3);
        mv.visitMethodInsn(183, "java/io/DataInputStream", "<init>", "(Ljava/io/InputStream;)V", false);
        mv.visitVarInsn(58, 4);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readInt", "()I", false);
        mv.visitInsn(87);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readInt", "()I", false);
        mv.visitInsn(87);
        final Label label4 = new Label();
        mv.visitLabel(label4);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "available", "()I", false);
        final Label label5 = new Label();
        mv.visitJumpInsn(158, label5);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readFloat", "()F", false);
        mv.visitLdcInsn(new Float("4.0"));
        mv.visitInsn(106);
        mv.visitInsn(139);
        mv.visitVarInsn(54, 5);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readUTF", "()Ljava/lang/String;", false);
        mv.visitVarInsn(58, 6);
        mv.visitVarInsn(21, 5);
        mv.visitIntInsn(188, 5);
        mv.visitVarInsn(58, 7);
        mv.visitInsn(3);
        mv.visitVarInsn(54, 8);
        final Label label6 = new Label();
        mv.visitLabel(label6);
        mv.visitVarInsn(21, 8);
        mv.visitVarInsn(21, 5);
        final Label label7 = new Label();
        mv.visitJumpInsn(162, label7);
        mv.visitVarInsn(25, 7);
        mv.visitVarInsn(21, 8);
        mv.visitVarInsn(25, 6);
        mv.visitVarInsn(21, 8);
        mv.visitMethodInsn(182, "java/lang/String", "charAt", "(I)C", false);
        mv.instructions.add(AsmUtils.pushInt(randomKey));
        mv.visitInsn(130);
        mv.visitVarInsn(21, 5);
        mv.visitInsn(130);
        mv.visitInsn(146);
        mv.visitInsn(85);
        mv.visitVarInsn(25, 7);
        mv.visitVarInsn(21, 8);
        mv.visitInsn(92);
        mv.visitInsn(52);
        mv.visitInsn(5);
        mv.visitInsn(122);
        mv.visitInsn(146);
        mv.visitInsn(85);
        mv.visitIincInsn(8, 1);
        mv.visitJumpInsn(167, label6);
        mv.visitLabel(label7);
        mv.visitTypeInsn(187, "java/lang/String");
        mv.visitInsn(89);
        mv.visitVarInsn(25, 7);
        mv.visitMethodInsn(183, "java/lang/String", "<init>", "([C)V", false);
        mv.visitVarInsn(58, 6);
        mv.visitIntInsn(16, 16);
        mv.visitIntInsn(188, 8);
        mv.visitVarInsn(58, 8);
        mv.visitVarInsn(25, 4);
        mv.visitVarInsn(25, 8);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readFully", "([B)V", false);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readInt", "()I", false);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readInt", "()I", false);
        mv.visitInsn(130);
        mv.visitIntInsn(188, 8);
        mv.visitVarInsn(58, 9);
        mv.visitVarInsn(25, 4);
        mv.visitVarInsn(25, 9);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "readFully", "([B)V", false);
        mv.visitLdcInsn("AES/ECB/PKCS5Padding");
        mv.visitMethodInsn(184, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;", false);
        mv.visitVarInsn(58, 10);
        mv.visitTypeInsn(187, "javax/crypto/spec/SecretKeySpec");
        mv.visitInsn(89);
        mv.visitVarInsn(25, 8);
        mv.visitLdcInsn("AES");
        mv.visitMethodInsn(183, "javax/crypto/spec/SecretKeySpec", "<init>", "([BLjava/lang/String;)V", false);
        mv.visitVarInsn(58, 11);
        mv.visitVarInsn(25, 10);
        mv.visitInsn(5);
        mv.visitVarInsn(25, 11);
        mv.visitMethodInsn(182, "javax/crypto/Cipher", "init", "(ILjava/security/Key;)V", false);
        mv.visitVarInsn(25, 10);
        mv.visitVarInsn(25, 9);
        mv.visitMethodInsn(182, "javax/crypto/Cipher", "doFinal", "([B)[B", false);
        mv.visitVarInsn(58, 9);
        mv.visitTypeInsn(187, "java/lang/Object");
        mv.visitInsn(89);
        mv.visitMethodInsn(183, "java/lang/Object", "<init>", "()V", false);
        mv.visitVarInsn(58, 12);
        mv.visitMethodInsn(184, "net/minecraftforge/fml/common/Loader", "instance", "()Lnet/minecraftforge/fml/common/Loader;", false);
        mv.visitMethodInsn(182, "net/minecraftforge/fml/common/Loader", "getModClassLoader", "()Lnet/minecraftforge/fml/common/ModClassLoader;", false);
        mv.visitVarInsn(58, 12);
        mv.visitLdcInsn(Type.getType("Lnet/minecraftforge/fml/common/ModClassLoader;"));
        mv.visitVarInsn(25, 12);
        mv.visitTypeInsn(192, "net/minecraftforge/fml/common/ModClassLoader");
        mv.visitInsn(4);
        mv.visitTypeInsn(189, "java/lang/String");
        mv.visitInsn(89);
        mv.visitInsn(3);
        mv.visitLdcInsn("mainClassLoader");
        mv.visitInsn(83);
        mv.visitMethodInsn(184, "net/minecraftforge/fml/relauncher/ReflectionHelper", "getPrivateValue", "(Ljava/lang/Class;Ljava/lang/Object;[Ljava/lang/String;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(192, "net/minecraft/launchwrapper/LaunchClassLoader");
        mv.visitVarInsn(58, 12);
        mv.visitLdcInsn(Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
        mv.visitVarInsn(25, 12);
        mv.visitTypeInsn(192, "net/minecraft/launchwrapper/LaunchClassLoader");
        mv.visitInsn(4);
        mv.visitTypeInsn(189, "java/lang/String");
        mv.visitInsn(89);
        mv.visitInsn(3);
        mv.visitLdcInsn("resourceCache");
        mv.visitInsn(83);
        mv.visitMethodInsn(184, "net/minecraftforge/fml/relauncher/ReflectionHelper", "getPrivateValue", "(Ljava/lang/Class;Ljava/lang/Object;[Ljava/lang/String;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(192, "java/util/concurrent/ConcurrentHashMap");
        mv.visitInsn(89);
        mv.visitVarInsn(25, 6);
        mv.visitVarInsn(25, 9);
        mv.visitMethodInsn(182, "java/util/concurrent/ConcurrentHashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitInsn(87);
        mv.visitLdcInsn(Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
        mv.visitInsn(95);
        mv.visitVarInsn(25, 12);
        mv.visitTypeInsn(192, "net/minecraft/launchwrapper/LaunchClassLoader");
        mv.visitInsn(95);
        mv.visitInsn(4);
        mv.visitTypeInsn(189, "java/lang/String");
        mv.visitInsn(89);
        mv.visitInsn(3);
        mv.visitLdcInsn("resourceCache");
        mv.visitInsn(83);
        mv.visitMethodInsn(184, "net/minecraftforge/fml/relauncher/ReflectionHelper", "setPrivateValue", "(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/String;)V", false);
        mv.visitJumpInsn(167, label4);
        mv.visitLabel(label5);
        mv.visitVarInsn(25, 4);
        mv.visitMethodInsn(182, "java/io/DataInputStream", "close", "()V", false);
        mv.visitVarInsn(25, 3);
        mv.visitMethodInsn(182, "java/io/ByteArrayInputStream", "close", "()V", false);
        mv.visitLabel(label2);
        final Label label8 = new Label();
        mv.visitJumpInsn(167, label8);
        mv.visitLabel(label3);
        mv.visitVarInsn(58, 3);
        mv.visitVarInsn(25, 3);
        mv.visitInsn(191);
        mv.visitLabel(label8);
        mv.visitTypeInsn(187, mainClass.name);
        mv.visitInsn(89);
        mv.visitMethodInsn(183, mainClass.name, "<init>", "()V");
        mv.visitFieldInsn(179, cw.name, "instance", "L" + mainClass.name + ";");
        mv.visitInsn(177);
        mv.visitMaxs(4, 13);
        mv.visitEnd();
    }
    
    @Override
    public String getSection() {
        return "misc.packer";
    }
    
    public byte[] pack(final int randomKey) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(bos);
            final Map<String, byte[]> entries = new HashMap<String, byte[]>();
            for (final ClassNode classNode : this.obf.getClasses()) {
                classNode.sourceDebug = null;
                classNode.sourceFile = null;
                final ContextClassWriter writer = new ContextClassWriter(2);
                classNode.accept(writer);
                final byte[] data = writer.toByteArray();
                entries.put(classNode.name, data);
                this.obf.addGeneratedClass(classNode.name, new byte[0]);
            }
            entries.putAll(this.obf.getResources());
            dos.writeInt(-889275714);
            dos.writeInt(52);
            for (final Map.Entry<String, byte[]> entry : entries.entrySet()) {
                byte[] data2 = entry.getValue();
                char[] name = entry.getKey().replace("/", ".").toCharArray();
                dos.writeFloat(name.length / 4.0f);
                for (int i = 0; i < name.length; ++i) {
                    final char[] array = name;
                    final int n = i;
                    array[n] <<= 2;
                    final char[] array2 = name;
                    final int n2 = i;
                    array2[n2] ^= (char)(randomKey ^ name.length);
                }
                for (int i = 0; i < this.random.nextInt(0, 1600); ++i) {
                    final char[] name2 = new char[name.length + 1];
                    System.arraycopy(name, 0, name2, 0, name.length);
                    name2[name.length] = (char)this.random.nextInt(0, 256);
                    name = name2;
                }
                dos.writeUTF(new String(name));
                final byte[] keyBytes = new byte[16];
                this.random.nextBytes(keyBytes);
                final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                final SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
                cipher.init(1, secretKeySpec);
                data2 = cipher.doFinal(data2);
                dos.write(keyBytes);
                final int l1 = data2.length;
                final int random = this.random.nextInt();
                dos.writeInt(l1 ^ random);
                dos.writeInt(random);
                dos.write(data2);
            }
            dos.close();
            bos.close();
            return bos.toByteArray();
        }
        catch (Throwable $ex) {
            throw new RuntimeException("Packer");
        }
    }
    
    public void unpack(final byte[] bytes, final int randomKey) {
        try {
            final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            final DataInputStream dis = new DataInputStream(bis);
            dis.readInt();
            dis.readInt();
            while (dis.available() > 0) {
                final int realLength = (int)(dis.readFloat() * 4.0f);
                String name = dis.readUTF();
                final char[] cname = new char[realLength];
                for (int i = 0; i < realLength; ++i) {
                    cname[i] = (char)(name.charAt(i) ^ randomKey ^ realLength);
                    final char[] array = cname;
                    final int n = i;
                    array[n] >>= 2;
                }
                name = new String(cname);
                final byte[] keyBytes = new byte[16];
                dis.readFully(keyBytes);
                byte[] data = new byte[dis.readInt() ^ dis.readInt()];
                dis.readFully(data);
                final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                final SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
                cipher.init(2, secretKeySpec);
                data = cipher.doFinal(data);
                if (isClass(data)) {
                    System.out.println("Class: " + name);
                }
                else {
                    System.out.println("Resource: " + name);
                }
            }
            dis.close();
            bis.close();
        }
        catch (Throwable $ex) {
            throw new RuntimeException("Packer");
        }
    }
}
