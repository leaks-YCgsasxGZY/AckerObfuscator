package cn.AckerRun.Ackerobfuscator.utils.asm;

import org.objectweb.asm.*;
import java.util.*;
import cn.AckerRun.Ackerobfuscator.utils.tree.*;

public class ContextClassWriter extends ClassWriter
{
    public ContextClassWriter(final int flags) {
        super(null, flags);
    }
    
    public ContextClassWriter(final int flags, final boolean b, final List<String> l) {
        super(null, flags, b, l);
    }
    
    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        return HierarchyUtils.getCommonSuperClass1(type1, type2);
    }
}
