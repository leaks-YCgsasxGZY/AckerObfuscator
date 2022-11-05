package cn.AckerRun.Ackerobfuscator.utils.configuration.file;

import org.apache.commons.lang3.*;
import com.google.common.io.*;
import org.apache.commons.io.*;
import java.nio.charset.*;
import java.io.*;
import cn.AckerRun.Ackerobfuscator.utils.configuration.*;
import org.yaml.snakeyaml.external.biz.base64Coder.*;

public abstract class FileConfiguration extends MemoryConfiguration
{
    @Deprecated
    public static final boolean UTF8_OVERRIDE;
    @Deprecated
    public static final boolean UTF_BIG;
    @Deprecated
    public static final boolean SYSTEM_UTF;
    
    public FileConfiguration() {
    }
    
    public FileConfiguration(final Configuration defaults) {
        super(defaults);
    }
    
    public void save(final File file) throws IOException {
        Validate.notNull(file, "File cannot be null", new Object[0]);
        Files.createParentDirs(file);
        final String data = this.saveToString();
        final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), (FileConfiguration.UTF8_OVERRIDE && !FileConfiguration.UTF_BIG) ? Charsets.UTF_8 : Charset.defaultCharset());
        try {
            writer.write(data);
        }
        finally {
            writer.close();
        }
    }
    
    public void save(final String file) throws IOException {
        Validate.notNull(file, "File cannot be null", new Object[0]);
        this.save(new File(file));
    }
    
    public abstract String saveToString();
    
    public void load(final File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null", new Object[0]);
        final FileInputStream stream = new FileInputStream(file);
        this.load(new InputStreamReader(stream, (FileConfiguration.UTF8_OVERRIDE && !FileConfiguration.UTF_BIG) ? Charsets.UTF_8 : Charset.defaultCharset()));
    }
    
    @Deprecated
    public void load(final InputStream stream) throws IOException, InvalidConfigurationException {
        Validate.notNull(stream, "Stream cannot be null", new Object[0]);
        this.load(new InputStreamReader(stream, FileConfiguration.UTF8_OVERRIDE ? Charsets.UTF_8 : Charset.defaultCharset()));
    }
    
    public void load(final Reader reader) throws IOException, InvalidConfigurationException {
        final BufferedReader input = (BufferedReader)((reader instanceof BufferedReader) ? reader : new BufferedReader(reader));
        final StringBuilder builder = new StringBuilder();
        try {
            String line;
            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        }
        finally {
            input.close();
        }
        this.loadFromString(builder.toString());
    }
    
    public void load(final String file) throws FileNotFoundException, IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null", new Object[0]);
        this.load(new File(file));
    }
    
    public abstract void loadFromString(final String p0) throws InvalidConfigurationException;
    
    protected abstract String buildHeader();
    
    @Override
    public FileConfigurationOptions options() {
        if (this.options == null) {
            this.options = new FileConfigurationOptions(this);
        }
        return (FileConfigurationOptions)this.options;
    }
    
    static {
        final byte[] testBytes = Base64Coder.decode("ICEiIyQlJicoKSorLC0uLzAxMjM0NTY3ODk6Ozw9Pj9AQUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVpbXF1eX2BhYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5ent8fX4NCg==");
        final String testString = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\r\n";
        final Charset defaultCharset = Charset.defaultCharset();
        final String resultString = new String(testBytes, defaultCharset);
        final boolean trueUTF = defaultCharset.name().contains("UTF");
        UTF8_OVERRIDE = (!" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\r\n".equals(resultString) || defaultCharset.equals(Charset.forName("US-ASCII")));
        SYSTEM_UTF = (trueUTF || FileConfiguration.UTF8_OVERRIDE);
        UTF_BIG = (trueUTF && FileConfiguration.UTF8_OVERRIDE);
    }
}
