package cn.AckerRun.Ackerobfuscator.utils.loader;

import java.security.*;
import java.util.*;
import java.util.concurrent.*;

public class LoaderUtil extends SecureClassLoader
{
    Map<String, byte[]> bytes;
    Map<String, Class<?>> classes;
    
    public LoaderUtil(final ClassLoader parent) {
        super(parent);
        this.bytes = new ConcurrentHashMap<String, byte[]>();
        this.classes = new ConcurrentHashMap<String, Class<?>>();
    }
    
    public Class<?> loadClass(final String name, final boolean resolve) {
        if (name.startsWith("java.")) {
            try {
                return super.loadClass(name, resolve);
            }
            catch (ClassNotFoundException e) {
                return null;
            }
        }
        if (this.bytes.containsKey(name)) {
            final byte[] b = this.bytes.get(name);
            final Class<?> klass = this.defineClass(name, b, 0, b.length);
            if (resolve) {
                this.resolveClass(klass);
            }
            this.classes.put(name, klass);
            this.bytes.remove(name);
        }
        if (this.classes.containsKey(name)) {
            return this.classes.get(name);
        }
        try {
            return super.loadClass(name, resolve);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    public void addClass(final String name, final byte[] bytes) {
        if (name.startsWith("java/lang")) {
            return;
        }
        this.bytes.put(name.replace("/", "."), bytes);
    }
}
