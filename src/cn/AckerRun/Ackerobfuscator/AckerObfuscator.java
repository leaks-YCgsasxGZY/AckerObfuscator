package cn.AckerRun.Ackerobfuscator;

import cn.AckerRun.Ackerobfuscator.transformer.Transformer;
import cn.AckerRun.Ackerobfuscator.transformer.flow.ExceptionTransformer;
import cn.AckerRun.Ackerobfuscator.transformer.general.StripTransformer;
import cn.AckerRun.Ackerobfuscator.transformer.methods.DynamicTransformer;
import cn.AckerRun.Ackerobfuscator.transformer.misc.ChecksumTransformer;
import cn.AckerRun.Ackerobfuscator.transformer.misc.PackerTransformer;
import cn.AckerRun.Ackerobfuscator.transformer.misc.VariableTransformer;
import cn.AckerRun.Ackerobfuscator.transformer.natives.CodeHiderTransformer;
import cn.AckerRun.Ackerobfuscator.transformer.natives.ConstantPoolTransformer;
import cn.AckerRun.Ackerobfuscator.transformer.strings.ToStringTransformer;
import cn.AckerRun.Ackerobfuscator.utils.asm.ClassWrapper;
import cn.AckerRun.Ackerobfuscator.utils.asm.ContextClassWriter;
import cn.AckerRun.Ackerobfuscator.utils.configuration.file.YamlConfiguration;
import cn.AckerRun.Ackerobfuscator.utils.loader.LoaderUtil;
import cn.AckerRun.Ackerobfuscator.utils.tree.ClassTree;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import sun.util.calendar.BaseCalendar;

public class AckerObfuscator implements Opcodes {
    private final Map<String, ClassTree> hierachy = new HashMap();
    private Manifest manifest;
    private static AckerObfuscator instance;
    private final ThreadLocalRandom random;
    private List<ClassWrapper> classes = new ArrayList();
    private final List<ClassWrapper> libs = new ArrayList(65525);
    private final List<Transformer> transformers = new ArrayList();
    private final YamlConfiguration config;
    private final HashMap<String, byte[]> resources = new HashMap();
    private final HashMap<String, byte[]> generated = new HashMap();
    private final LoaderUtil loader;
    private final Vector<String> libraries = new Vector();

    public List<Transformer> getTransformers() {
        return this.transformers;
    }

    public AckerObfuscator(YamlConfiguration configuration) throws Exception {
        this.config = configuration;
        this.loader = new LoaderUtil(AckerObfuscator.class.getClassLoader());
        File inputFile = new File(this.config.getString("input"));
        File outputFile = new File(this.config.getString("output"));
        List<File> libs = (List)this.config.getStringList("libs").stream().map(File::new).collect(Collectors.toList());
        instance = this;
        this.loadJavaRuntime();
        LinkedList<Thread> libraryThreads = new LinkedList();
        System.out.println("Loading libraries...");
        Iterator var6 = this.libraries.iterator();

        while(var6.hasNext()) {
            String library = (String)var6.next();
            libraryThreads.add(new Thread(() -> {
                try {
                    this.loadJar(new File(library), true);
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }));
        }

        var6 = libs.iterator();

        while(var6.hasNext()) {
            File folder = (File)var6.next();
            Iterator var8 = this.walkFolder(folder).iterator();

            while(var8.hasNext()) {
                File lib = (File)var8.next();
                if (!lib.getName().startsWith("rt")) {
                    libraryThreads.add(new Thread(() -> {
                        try {
                            this.loadJar(lib, true);
                        } catch (Exception var3) {
                            var3.printStackTrace();
                        }

                    }));
                }
            }
        }

        var6 = libraryThreads.iterator();

        Thread libraryThread;
        while(var6.hasNext()) {
            libraryThread = (Thread)var6.next();
            libraryThread.start();
        }

        var6 = libraryThreads.iterator();

        while(var6.hasNext()) {
            libraryThread = (Thread)var6.next();
            libraryThread.join();
        }

        System.out.println("Reading jar...");
        this.loadJar(inputFile, false);
        this.random = ThreadLocalRandom.current();
        System.out.println("Loading transformers...");
        this.transformers.add(new StripTransformer(this));
        this.transformers.add(new ExceptionTransformer(this));
        this.transformers.add(new VariableTransformer(this));
        this.transformers.add(new DynamicTransformer(this));
        this.transformers.add(new ToStringTransformer(this));
        this.transformers.add(new ChecksumTransformer(this));
        this.transformers.add(new ConstantPoolTransformer(this));
        this.transformers.add(new CodeHiderTransformer(this));
        this.transformers.add(new PackerTransformer(this));
        long start = System.currentTimeMillis();
        JarOutputStream out = new JarOutputStream(new FileOutputStream(outputFile));

        try {
            System.out.println("Transforming classes...");
            Iterator var28 = this.transformers.iterator();

            Transformer transformer;
            while(var28.hasNext()) {
                transformer = (Transformer)var28.next();
                if (transformer.enabled) {
                    ArrayList<ClassWrapper> var10000 = new ArrayList<>(this.classes);
                    Objects.requireNonNull(transformer);
                    var10000.forEach(transformer::run);
                }
            }

            var28 = this.transformers.iterator();

            label115:
            while(true) {
                do {
                    if (!var28.hasNext()) {
                        if (this.manifest != null) {
                            ZipEntry e = new ZipEntry("META-INF/MANIFEST.MF");
                            out.putNextEntry(e);
                            this.manifest.write(new BufferedOutputStream(out));
                            out.closeEntry();
                        }

                        System.out.println("Writing classes...");
                        var28 = this.classes.iterator();

                        while(true) {
                            byte[] b;
                            ClassWrapper classNode;
                            do {
                                if (!var28.hasNext()) {
                                    System.out.println("Writing resources...");
                                    this.resources.forEach((name, data) -> {
                                        try {
                                            if (name.equals("META-INF/MANIFEST.MF")) {
                                                return;
                                            }

                                            out.putNextEntry(new JarEntry(name));
                                            out.write(data);
                                        } catch (IOException var4) {
                                            var4.printStackTrace();
                                        }

                                    });
                                    out.close();
                                    long difference = outputFile.length() - inputFile.length();
                                    boolean compressed = difference < 0L;
                                    Date epoch = new Date(0L);
                                    Date elapsed = Date.from(Instant.ofEpochMilli(System.currentTimeMillis() - start));
                                    StringBuilder time = new StringBuilder();
                                    int dh = elapsed.getHours() - epoch.getHours();
                                    int dm = elapsed.getMinutes() - epoch.getMinutes();
                                    int ds = elapsed.getSeconds() - epoch.getSeconds();
                                    if (dh > 0) {
                                        time.append(dh).append("h ");
                                    }

                                    if (dm > 0) {
                                        time.append(dm).append("m ");
                                    }

                                    if (ds > 0) {
                                        time.append(ds).append("s ");
                                    }

                                    Method normalize = Date.class.getDeclaredMethod("normalize");
                                    normalize.setAccessible(true);
                                    BaseCalendar.Date date = (BaseCalendar.Date)normalize.invoke(elapsed);
                                    time.append(date.getMillis()).append("ms");
                                    System.out.printf("Size: %.2fKB -> %.2fKB (%s%.2f%%)\n", (double)inputFile.length() / 1024.0, (double)outputFile.length() / 1024.0, compressed ? "-" : "+", 100.0 * Math.abs((double)difference) / (double)inputFile.length());
                                    System.out.printf("Elapsed: %s\n", time);
                                    break label115;
                                }

                                classNode = (ClassWrapper)var28.next();
                                b = (byte[])this.generated.getOrDefault(classNode.name, null);
                            } while(b != null && b.length == 0);

                            if (b == null) {
                                ContextClassWriter writer = new ContextClassWriter(2);

                                try {
                                    classNode.accept(writer);
                                    b = writer.toByteArray();
                                } catch (Exception var21) {
                                    System.out.println("Failed to compute frames for class: " + classNode.name + ", " + var21.getMessage());
                                    writer = new ContextClassWriter(1);
                                    classNode.accept(writer);
                                    b = writer.toByteArray();
                                }
                            }

                            if (b != null) {
                                out.putNextEntry(new JarEntry(classNode.name + ".class"));
                                out.write(b);
                            }
                        }
                    }

                    transformer = (Transformer)var28.next();
                } while(!transformer.enabled);

                try {
                    transformer.getClass().getDeclaredMethod("after");
                } catch (NoSuchMethodException var22) {
                    continue;
                }

                transformer.runAfter();
            }
        } catch (Throwable var23) {
            try {
                out.close();
            } catch (Throwable var20) {
                var23.addSuppressed(var20);
            }

            throw var23;
        }

        out.close();
    }

    public HashMap<String, byte[]> getResources() {
        return this.resources;
    }

    public void loadJavaRuntime() {
        String path = System.getProperty("sun.boot.class.path");
        if (path != null) {
            String[] pathFiles = path.split(";");
            String[] var3 = pathFiles;
            int var4 = pathFiles.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String lib = var3[var5];
                if (lib.endsWith(".jar")) {
                    this.libraries.addElement(lib);
                }
            }
        }

    }

    public void loadJar(File inputFile, boolean lib) throws Exception {
        if (inputFile.exists()) {
            JarFile inputJar = new JarFile(inputFile);
            if (!lib) {
                this.manifest = inputJar.getManifest();
            }

            Enumeration<JarEntry> iter = inputJar.entries();

            while(true) {
                JarEntry entry;
                do {
                    if (!iter.hasMoreElements()) {
                        return;
                    }

                    entry = (JarEntry)iter.nextElement();
                } while(entry.isDirectory());

                InputStream in = inputJar.getInputStream(entry);

                try {
                    byte[] bytes = IOUtils.toByteArray(in);
                    if (!entry.getName().endsWith(".class") && !entry.getName().endsWith(".class/")) {
                        if (!lib) {
                            this.resources.put(entry.getName(), bytes);
                        }
                    } else {
                        ClassReader reader = new ClassReader(bytes);
                        ClassWrapper classNode = new ClassWrapper(!lib);
                        reader.accept(classNode, 4);
                        if (lib) {
                            this.libs.add(classNode);
                        } else {
                            this.classes.add(classNode);
                        }

                        this.loader.addClass(classNode.name, bytes);
                    }
                } catch (Throwable var11) {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable var10) {
                            var11.addSuppressed(var10);
                        }
                    }

                    throw var11;
                }

                if (in != null) {
                    in.close();
                }
            }
        }
    }

    private List<File> walkFolder(File folder) {
        List<File> files = new ArrayList();
        if (!folder.isDirectory()) {
            if (folder.getName().endsWith(".jar")) {
                files.add(folder);
            }

            return files;
        } else {
            File[] var3 = folder.listFiles();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                File file = var3[var5];
                if (file.isDirectory()) {
                    files.addAll(this.walkFolder(file));
                } else if (file.getName().endsWith(".jar")) {
                    files.add(file);
                }
            }

            return files;
        }
    }

    public void loadHierachy() {
        Set<String> processed = new HashSet();
        LinkedList<ClassWrapper> toLoad = new LinkedList(this.classes);
        toLoad.addAll(this.libs);

        while(!toLoad.isEmpty()) {
            Iterator var3 = this.loadHierachy((ClassWrapper)toLoad.poll()).iterator();

            while(var3.hasNext()) {
                ClassWrapper toProcess = (ClassWrapper)var3.next();
                if (processed.add(toProcess.name)) {
                    toLoad.add(toProcess);
                }
            }
        }

    }

    public ClassTree getClassTree(String classNode) {
        ClassTree tree = (ClassTree)this.hierachy.get(classNode);
        if (tree == null) {
            this.loadHierachyAll(this.assureLoaded(classNode));
            return this.getClassTree(classNode);
        } else {
            return tree;
        }
    }

    private ClassTree getOrCreateClassTree(String name) {
        return (ClassTree)this.hierachy.computeIfAbsent(name, ClassTree::new);
    }

    public List<ClassWrapper> loadHierachy(ClassWrapper specificNode) {
        if (specificNode.name.equals("java/lang/Object")) {
            return Collections.emptyList();
        } else {
            List<ClassWrapper> toProcess = new ArrayList();
            ClassTree thisTree = this.getOrCreateClassTree(specificNode.name);
            ClassWrapper superClass = this.assureLoaded(specificNode.superName);
            if (superClass == null) {
                throw new IllegalArgumentException("Could not load " + specificNode.name);
            } else {
                ClassTree superTree = this.getOrCreateClassTree(superClass.name);
                superTree.subClasses.add(specificNode.name);
                thisTree.parentClasses.add(superClass.name);
                toProcess.add(superClass);
                Iterator var6 = specificNode.interfaces.iterator();

                while(var6.hasNext()) {
                    String interfaceReference = (String)var6.next();
                    ClassWrapper interfaceNode = this.assureLoaded(interfaceReference);
                    if (interfaceNode == null) {
                        throw new IllegalArgumentException("Could not load " + interfaceReference);
                    }

                    ClassTree interfaceTree = this.getOrCreateClassTree(interfaceReference);
                    interfaceTree.subClasses.add(specificNode.name);
                    thisTree.parentClasses.add(interfaceReference);
                    toProcess.add(interfaceNode);
                }

                return toProcess;
            }
        }
    }

    public void loadHierachyAll(ClassWrapper classNode) {
        Set<String> processed = new HashSet();
        LinkedList<ClassWrapper> toLoad = new LinkedList();
        toLoad.add(classNode);

        while(!toLoad.isEmpty()) {
            Iterator var4 = this.loadHierachy((ClassWrapper)toLoad.poll()).iterator();

            while(var4.hasNext()) {
                ClassWrapper toProcess = (ClassWrapper)var4.next();
                if (processed.add(toProcess.name)) {
                    toLoad.add(toProcess);
                }
            }
        }

    }

    public YamlConfiguration getConfig() {
        return this.config;
    }

    public void addResource(String name, byte[] data) {
        this.resources.put(name, data);
    }

    public ThreadLocalRandom getRandom() {
        return this.random;
    }

    public void setClasses(List<ClassWrapper> classes) {
        this.classes = classes;
    }

    public List<ClassWrapper> getClasses() {
        return this.classes;
    }

    public List<ClassWrapper> getLibs() {
        return this.libs;
    }

    public void addClass(ClassWrapper classNode) {
        this.classes.add(classNode);
    }

    public ClassWrapper assureLoaded(String owner) {
        if (owner == null) {
            return null;
        } else {
            Iterator var2 = this.classes.iterator();

            ClassWrapper classNode;
            do {
                if (!var2.hasNext()) {
                    var2 = this.libs.iterator();

                    do {
                        if (!var2.hasNext()) {
                            return null;
                        }

                        classNode = (ClassWrapper)var2.next();
                    } while(classNode == null || !classNode.name.equals(owner));

                    return classNode;
                }

                classNode = (ClassWrapper)var2.next();
            } while(!classNode.name.equals(owner));

            return classNode;
        }
    }

    public Manifest getManifest() {
        return this.manifest;
    }

    public void addGeneratedClass(String name, byte[] b) {
        this.generated.put(name, b);
    }

    public boolean isTransformerEnabled(Class<? extends Transformer> transformer) {
        return this.transformers.stream().anyMatch((t) -> {
            return t.getClass().equals(transformer) && t.enabled;
        });
    }

    public static AckerObfuscator getInstance() {
        return instance;
    }

    public LoaderUtil getLoader() {
        return this.loader;
    }
}
