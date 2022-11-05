package cn.AckerRun.Ackerobfuscator.utils.tree;

import java.util.*;

public class ClassTree
{
    public String thisClass;
    public Set<String> subClasses;
    public Set<String> parentClasses;
    
    public ClassTree(final String thisClass) {
        this.subClasses = new HashSet<String>();
        this.parentClasses = new HashSet<String>();
        this.thisClass = thisClass;
    }
}
