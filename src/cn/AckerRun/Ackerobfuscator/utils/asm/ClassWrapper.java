package cn.AckerRun.Ackerobfuscator.utils.asm;

import org.objectweb.asm.tree.*;
import org.objectweb.asm.*;

public class ClassWrapper extends ClassNode
{
    public boolean modify;
    
    public ClassWrapper(final boolean modify) {
        super(589824);
        this.modify = modify;
    }
    
    public ContextClassWriter createWriter() {
        return this.createWriter(2);
    }
    
    public ContextClassWriter createWriter(final int flags) {
        final ContextClassWriter cw = new ContextClassWriter(flags);
        this.accept(cw);
        return cw;
    }
}
