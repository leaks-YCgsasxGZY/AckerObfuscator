package cn.AckerRun.Ackerobfuscator.transformer.general;

import cn.AckerRun.Ackerobfuscator.transformer.*;
import cn.AckerRun.Ackerobfuscator.*;
import cn.AckerRun.Ackerobfuscator.utils.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;

public class StripTransformer extends Transformer
{
    public StripTransformer(final AckerObfuscator obf) {
        super(obf);
    }
    
    @Override
    public String getSection() {
        return "general.strip";
    }
    
    @Override
    protected void after() {
        for (final ClassWrapper classNode : this.obf.getClasses()) {
            for (final MethodNode method : classNode.methods) {
                method.localVariables = null;
                method.parameters = null;
                method.signature = null;
                for (final AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof LineNumberNode) {
                        method.instructions.remove(instruction);
                    }
                }
            }
            for (final FieldNode field : classNode.fields) {
                field.signature = null;
            }
            classNode.signature = null;
            classNode.innerClasses.clear();
            classNode.sourceFile = null;
            classNode.sourceDebug = null;
        }
    }
}
