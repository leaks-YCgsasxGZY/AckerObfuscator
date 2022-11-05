package cn.AckerRun.Ackerobfuscator.transformer;

import cn.AckerRun.Ackerobfuscator.*;
import java.util.concurrent.*;
import cn.AckerRun.Ackerobfuscator.utils.configuration.*;
import cn.AckerRun.Ackerobfuscator.utils.pair.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import cn.AckerRun.Ackerobfuscator.transformer.natives.*;
import org.apache.commons.io.*;
import java.io.*;
import org.objectweb.asm.*;
import java.util.*;
import org.objectweb.asm.tree.*;

public abstract class Transformer implements Opcodes
{
    protected final AckerObfuscator obf;
    protected final ThreadLocalRandom random;
    protected final ConfigurationSection config;
    protected int iterations;
    public static boolean loadedNative;
    protected Vector<String> excluded;
    protected Vector<String> included;
    public boolean enabled;
    public ClassMethodNode target;
    public boolean canBeIterated;
    
    public abstract String getSection();
    
    public Transformer(final AckerObfuscator obf) {
        this.iterations = 1;
        this.excluded = new Vector<String>();
        this.included = new Vector<String>();
        this.canBeIterated = true;
        this.obf = obf;
        this.random = obf.getRandom();
        this.config = obf.getConfig().getConfigurationSection(this.getSection());
        this.enabled = this.config.getBoolean("enabled", true);
        this.excluded.addAll(this.config.getStringList("excluded"));
        this.included.addAll(this.config.getStringList("included"));
        this.iterations = this.config.getInt("iterations", 1);
        Transformer.loadedNative = obf.getClasses().stream().anyMatch(c -> c.name.equals("vm/NativeHandler"));
    }
    
    public final void run(final ClassWrapper classNode) {
        try {
            if (!this.enabled) {
                return;
            }
            for (final String s : this.excluded) {
                if (classNode.name.startsWith(s)) {
                    return;
                }
            }
            for (final String s : this.included) {
                if (!classNode.name.startsWith(s)) {
                    return;
                }
            }
            for (int i = 0; i < this.iterations; ++i) {
                this.visit(classNode);
            }
        }
        catch (Throwable $ex) {
            throw $ex;
        }
    }
    
    protected void visit(final ClassWrapper classNode) {
    }
    
    public final void runAfter() {
        try {
            if (!this.enabled) {
                return;
            }
            if ((this.obf.isTransformerEnabled(ConstantPoolTransformer.class) || this.obf.isTransformerEnabled(CodeHiderTransformer.class)) && !Transformer.loadedNative) {
                final File file = new File("target\\classes\\vm\\NativeHandler.class");
                final byte[] b = IOUtils.toByteArray(new FileInputStream(file));
                final ClassReader cr = new ClassReader(b);
                final ClassWrapper cw = new ClassWrapper(false);
                cr.accept(cw, 2);
                cw.name = "vm/NativeHandler";
                cw.methods.removeIf(m -> m.name.startsWith("raw_"));
                this.obf.addClass(cw);
                Transformer.loadedNative = true;
            }
            final List<ClassWrapper> clone = new ArrayList<ClassWrapper>(this.obf.getClasses());
            for (final ClassWrapper classNode : clone) {
                for (final String s : this.excluded) {
                    if (classNode.name.startsWith(s)) {
                        this.obf.getClasses().remove(classNode);
                    }
                }
                for (final String s : this.included) {
                    if (!classNode.name.startsWith(s)) {
                        this.obf.getClasses().remove(classNode);
                    }
                }
            }
            this.after();
            this.obf.setClasses(clone);
        }
        catch (Throwable $ex) {
            throw new RuntimeException("Transformer");
        }
    }
    
    protected void after() {
    }
    
    protected boolean nextBoolean(final int i) {
        boolean ret = this.random.nextBoolean();
        for (int j = 0; j < i; ++j) {
            ret = (this.random.nextBoolean() && ret);
        }
        return ret;
    }
    
    protected void error(final String message, final Object... args) {
        System.err.printf("[" + this.getClass().getSimpleName() + "] " + message + "\n", args);
    }
    
    protected void log(final String message, final Object... args) {
        System.out.printf("[" + this.getClass().getSimpleName() + "] " + message + "\n", args);
    }
}
