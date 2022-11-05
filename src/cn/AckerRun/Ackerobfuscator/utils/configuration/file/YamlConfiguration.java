package cn.AckerRun.Ackerobfuscator.utils.configuration.file;

import org.yaml.snakeyaml.representer.*;
import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.*;
import org.apache.commons.lang3.*;
import org.yaml.snakeyaml.error.*;
import java.util.*;
import java.io.*;
import cn.AckerRun.Ackerobfuscator.utils.configuration.*;

public class YamlConfiguration extends FileConfiguration
{
    protected static final String COMMENT_PREFIX = "# ";
    protected static final String BLANK_CONFIG = "{}\n";
    private final DumperOptions yamlOptions;
    private final Representer yamlRepresenter;
    private final Yaml yaml;
    
    public YamlConfiguration() {
        this.yamlOptions = new DumperOptions();
        this.yamlRepresenter = new YamlRepresenter();
        this.yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions);
    }
    
    @Override
    public String saveToString() {
        this.yamlOptions.setIndent(this.options().indent());
        this.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yamlOptions.setAllowUnicode(YamlConfiguration.SYSTEM_UTF);
        this.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        final String header = this.buildHeader();
        String dump = this.yaml.dump(this.getValues(false));
        if (dump.equals("{}\n")) {
            dump = "";
        }
        return header + dump;
    }
    
    @Override
    public void loadFromString(final String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null", new Object[0]);
        Map input;
        try {
            input = this.yaml.load(contents);
        }
        catch (YAMLException var4) {
            throw new InvalidConfigurationException(var4);
        }
        catch (ClassCastException var5) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }
        final String header = this.parseHeader(contents);
        if (header.length() > 0) {
            this.options().header(header);
        }
        if (input != null) {
            this.convertMapsToSections(input, this);
        }
    }
    
    protected void convertMapsToSections(final Map<?, ?> input, final ConfigurationSection section) {
        for (final Map.Entry<?, ?> entry : input.entrySet()) {
            final String key = entry.getKey().toString();
            final Object value = entry.getValue();
            if (value instanceof Map) {
                this.convertMapsToSections((Map<?, ?>)value, section.createSection(key));
            }
            else {
                section.set(key, value);
            }
        }
    }
    
    protected String parseHeader(final String input) {
        final String[] lines = input.split("\r?\n", -1);
        final StringBuilder result = new StringBuilder();
        boolean readingHeader = true;
        boolean foundHeader = false;
        for (int i = 0; i < lines.length && readingHeader; ++i) {
            final String line = lines[i];
            if (line.startsWith("# ")) {
                if (i > 0) {
                    result.append("\n");
                }
                if (line.length() > "# ".length()) {
                    result.append(line.substring("# ".length()));
                }
                foundHeader = true;
            }
            else if (foundHeader && line.length() == 0) {
                result.append("\n");
            }
            else if (foundHeader) {
                readingHeader = false;
            }
        }
        return result.toString();
    }
    
    @Override
    protected String buildHeader() {
        final String header = this.options().header();
        if (this.options().copyHeader()) {
            final Configuration def = this.getDefaults();
            if (def != null && def instanceof FileConfiguration) {
                final FileConfiguration filedefaults = (FileConfiguration)def;
                final String defaultsHeader = filedefaults.buildHeader();
                if (defaultsHeader != null && defaultsHeader.length() > 0) {
                    return defaultsHeader;
                }
            }
        }
        if (header == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        final String[] lines = header.split("\r?\n", -1);
        boolean startedHeader = false;
        for (int i = lines.length - 1; i >= 0; --i) {
            builder.insert(0, "\n");
            if (startedHeader || lines[i].length() != 0) {
                builder.insert(0, lines[i]);
                builder.insert(0, "# ");
                startedHeader = true;
            }
        }
        return builder.toString();
    }
    
    @Override
    public YamlConfigurationOptions options() {
        if (this.options == null) {
            this.options = new YamlConfigurationOptions(this);
        }
        return (YamlConfigurationOptions)this.options;
    }
    
    public static YamlConfiguration loadConfiguration(final File file) {
        Validate.notNull(file, "File cannot be null", new Object[0]);
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        }
        catch (IOException | InvalidConfigurationException ex2) {
            System.err.println("Cannot load configuration from " + file);
            ex2.printStackTrace();
        }
        return config;
    }
    
    @Deprecated
    public static YamlConfiguration loadConfiguration(final InputStream stream) {
        Validate.notNull(stream, "Stream cannot be null", new Object[0]);
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(stream);
        }
        catch (IOException | InvalidConfigurationException ex2) {
            System.err.println("Cannot load configuration from stream");
            ex2.printStackTrace();
        }
        return config;
    }
    
    public static YamlConfiguration loadConfiguration(final Reader reader) {
        Validate.notNull(reader, "Stream cannot be null", new Object[0]);
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(reader);
        }
        catch (IOException | InvalidConfigurationException ex2) {
            System.err.println("Cannot load configuration from stream");
            ex2.printStackTrace();
        }
        return config;
    }
}
