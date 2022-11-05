package cn.AckerRun.Ackerobfuscator.utils.configuration.file;

import org.apache.commons.lang3.*;
import cn.AckerRun.Ackerobfuscator.utils.configuration.*;

public class YamlConfigurationOptions extends FileConfigurationOptions
{
    private int indent;
    
    protected YamlConfigurationOptions(final YamlConfiguration configuration) {
        super(configuration);
        this.indent = 2;
    }
    
    @Override
    public YamlConfiguration configuration() {
        return (YamlConfiguration)super.configuration();
    }
    
    @Override
    public YamlConfigurationOptions copyDefaults(final boolean value) {
        super.copyDefaults(value);
        return this;
    }
    
    @Override
    public YamlConfigurationOptions pathSeparator(final char value) {
        super.pathSeparator(value);
        return this;
    }
    
    @Override
    public YamlConfigurationOptions header(final String value) {
        super.header(value);
        return this;
    }
    
    @Override
    public YamlConfigurationOptions copyHeader(final boolean value) {
        super.copyHeader(value);
        return this;
    }
    
    public int indent() {
        return this.indent;
    }
    
    public YamlConfigurationOptions indent(final int value) {
        Validate.isTrue(value >= 2, "Indent must be at least 2 characters", new Object[0]);
        Validate.isTrue(value <= 9, "Indent cannot be greater than 9 characters", new Object[0]);
        this.indent = value;
        return this;
    }
}
