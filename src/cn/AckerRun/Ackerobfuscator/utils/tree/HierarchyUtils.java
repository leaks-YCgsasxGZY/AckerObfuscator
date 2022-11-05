package cn.AckerRun.Ackerobfuscator.utils.tree;

import cn.AckerRun.Ackerobfuscator.*;
import java.lang.reflect.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import java.util.*;

public class HierarchyUtils
{
    private static AckerObfuscator obf;
    
    public static String getCommonSuperClass0(String type1, final String type2) {
        ClassWrapper first = HierarchyUtils.obf.assureLoaded(type1);
        final ClassWrapper second = HierarchyUtils.obf.assureLoaded(type2);
        if (isAssignableFrom(type1, type2)) {
            return type1;
        }
        if (isAssignableFrom(type2, type1)) {
            return type2;
        }
        if (Modifier.isInterface(first.access) || Modifier.isInterface(second.access)) {
            return "java/lang/Object";
        }
        do {
            type1 = first.superName;
            first = HierarchyUtils.obf.assureLoaded(type1);
        } while (!isAssignableFrom(type1, type2));
        return type1;
    }
    
    public static String getCommonSuperClass1(final String type1, final String type2) {
        if ("java/lang/Object".equals(type1) || "java/lang/Object".equals(type2)) {
            return "java/lang/Object";
        }
        final String a = getCommonSuperClass0(type1, type2);
        final String b = getCommonSuperClass0(type2, type1);
        if (!"java/lang/Object".equals(a)) {
            return a;
        }
        if (!"java/lang/Object".equals(b)) {
            return b;
        }
        final ClassWrapper first = HierarchyUtils.obf.assureLoaded(type1);
        final ClassWrapper second = HierarchyUtils.obf.assureLoaded(type2);
        return getCommonSuperClass1(first.superName, second.superName);
    }
    
    public static boolean isAssignableFrom(final String type1, final String type2) {
        if ("java/lang/Object".equals(type1)) {
            return true;
        }
        if (type1.equals(type2)) {
            return true;
        }
        HierarchyUtils.obf.assureLoaded(type1);
        HierarchyUtils.obf.assureLoaded(type2);
        final ClassTree firstTree = HierarchyUtils.obf.getClassTree(type1);
        final Set<String> allChilds1 = new HashSet<String>();
        final LinkedList<String> toProcess = new LinkedList<String>(firstTree.subClasses);
        while (!toProcess.isEmpty()) {
            final String s = toProcess.poll();
            if (allChilds1.add(s)) {
                HierarchyUtils.obf.assureLoaded(s);
                final ClassTree tempTree = HierarchyUtils.obf.getClassTree(s);
                toProcess.addAll(tempTree.subClasses);
            }
        }
        return allChilds1.contains(type2);
    }
    
    static {
        HierarchyUtils.obf = AckerObfuscator.getInstance();
    }
}
