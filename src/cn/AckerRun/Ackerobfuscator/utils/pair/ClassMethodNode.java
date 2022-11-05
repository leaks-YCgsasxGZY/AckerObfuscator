package cn.AckerRun.Ackerobfuscator.utils.pair;

import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import org.objectweb.asm.tree.*;

public class ClassMethodNode
{
    private final ClassWrapper classNode;
    private final MethodNode methodNode;
    
    public ClassMethodNode(final ClassWrapper classNode, final MethodNode methodNode) {
        this.classNode = classNode;
        this.methodNode = methodNode;
    }
    
    public ClassWrapper getClassWrapper() {
        return this.classNode;
    }
    
    public MethodNode getMethodNode() {
        return this.methodNode;
    }
    
    @Override
    public String toString() {
        return this.classNode.name + "." + this.methodNode.name + this.methodNode.desc;
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o != null && this.getClass() == o.getClass() && this.toString().equals(o.toString()));
    }
    
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
