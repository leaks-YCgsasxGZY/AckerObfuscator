package cn.AckerRun.Ackerobfuscator.addons.skidfuscator;

import cn.AckerRun.Ackerobfuscator.addons.*;
import java.io.*;
import java.util.*;

public class SkidfuscatorAddon implements IObfuscator
{
    @Override
    public void transform(final File input, final File output, final List<File> includes) {
        final StringBuilder args = new StringBuilder();
        args.append("-ph ").append("-li=").append(includes.toString(), 1, includes.toString().length() - 1).append(input.getAbsolutePath()).append(" -o=").append(output.getAbsolutePath());
    }
}
