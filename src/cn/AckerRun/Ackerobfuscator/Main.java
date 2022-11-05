package cn.AckerRun.Ackerobfuscator;

import cn.AckerRun.Ackerobfuscator.utils.configuration.file.YamlConfiguration;
import java.io.File;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        System.out.println("Starting...");
        OptionParser parser = new OptionParser();
        parser.accepts("config").withRequiredArg().required().ofType(File.class);

        OptionSet options;
        try {
            options = parser.parse(args);
        } catch (OptionException var7) {
            System.out.println("Usage: obf --config <config>");
            System.out.println(var7.getMessage());
            System.exit(1);
            return;
        }

        File configFile = (File)options.valueOf("config");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        try {
            new AckerObfuscator(config);
        } catch (Exception var6) {
            var6.printStackTrace();
            System.exit(1);
        }

    }
}
