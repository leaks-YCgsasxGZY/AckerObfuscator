package cn.AckerRun.Ackerobfuscator.utils.configuration;

import org.apache.commons.lang3.*;
import java.util.*;

public class MemoryConfiguration extends MemorySection implements Configuration
{
    protected Configuration defaults;
    protected MemoryConfigurationOptions options;
    
    public MemoryConfiguration() {
    }
    
    public MemoryConfiguration(final Configuration defaults) {
        this.defaults = defaults;
    }
    
    @Override
    public void addDefault(final String path, final Object value) {
        Validate.notNull(path, "Path may not be null", new Object[0]);
        if (this.defaults == null) {
            this.defaults = new MemoryConfiguration();
        }
        this.defaults.set(path, value);
    }
    
    @Override
    public void addDefaults(final Map<String, Object> defaults) {
        Validate.notNull(defaults, "Defaults may not be null", new Object[0]);
        for (final Map.Entry<String, Object> entry : defaults.entrySet()) {
            this.addDefault(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public void addDefaults(final Configuration defaults) {
        Validate.notNull(defaults, "Defaults may not be null", new Object[0]);
        this.addDefaults(defaults.getValues(true));
    }
    
    @Override
    public void setDefaults(final Configuration defaults) {
        Validate.notNull(defaults, "Defaults may not be null", new Object[0]);
        this.defaults = defaults;
    }
    
    @Override
    public Configuration getDefaults() {
        return this.defaults;
    }
    
    @Override
    public ConfigurationSection getParent() {
        return null;
    }
    
    @Override
    public MemoryConfigurationOptions options() {
        if (this.options == null) {
            this.options = new MemoryConfigurationOptions(this);
        }
        return this.options;
    }
}
