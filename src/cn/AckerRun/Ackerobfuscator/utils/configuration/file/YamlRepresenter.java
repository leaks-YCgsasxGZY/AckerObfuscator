package cn.AckerRun.Ackerobfuscator.utils.configuration.file;

import cn.AckerRun.Ackerobfuscator.utils.configuration.*;
import org.yaml.snakeyaml.representer.*;
import org.yaml.snakeyaml.nodes.*;
import cn.AckerRun.Ackerobfuscator.utils.configuration.serialization.*;
import java.util.*;

public class YamlRepresenter extends Representer
{
    public YamlRepresenter() {
        this.multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
        this.multiRepresenters.put(ConfigurationSerializable.class, new RepresentConfigurationSerializable());
    }
    
    private class RepresentConfigurationSection extends RepresentMap
    {
        @Override
        public Node representData(final Object data) {
            return super.representData(((ConfigurationSection)data).getValues(false));
        }
    }
    
    private class RepresentConfigurationSerializable extends RepresentMap
    {
        @Override
        public Node representData(final Object data) {
            final ConfigurationSerializable serializable = (ConfigurationSerializable)data;
            final Map<String, Object> values = new LinkedHashMap<String, Object>();
            values.put("==", ConfigurationSerialization.getAlias(serializable.getClass()));
            values.putAll(serializable.serialize());
            return super.representData(values);
        }
    }
}
