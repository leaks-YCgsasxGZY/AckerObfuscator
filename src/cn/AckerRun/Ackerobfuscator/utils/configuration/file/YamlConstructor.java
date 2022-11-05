package cn.AckerRun.Ackerobfuscator.utils.configuration.file;

import org.yaml.snakeyaml.constructor.*;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.error.*;
import cn.AckerRun.Ackerobfuscator.utils.configuration.serialization.*;
import java.util.*;

public class YamlConstructor extends SafeConstructor
{
    public YamlConstructor() {
        this.yamlConstructors.put(Tag.MAP, new ConstructCustomObject());
    }
    
    private class ConstructCustomObject extends ConstructYamlMap
    {
        @Override
        public Object construct(final Node node) {
            if (node.isTwoStepsConstruction()) {
                throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
            }
            final Map<?, ?> raw = (Map<?, ?>)super.construct(node);
            if (!raw.containsKey("==")) {
                return raw;
            }
            final Map<String, Object> typed = new LinkedHashMap<String, Object>(raw.size());
            for (final Map.Entry<?, ?> entry : raw.entrySet()) {
                typed.put(entry.getKey().toString(), entry.getValue());
            }
            try {
                return ConfigurationSerialization.deserializeObject(typed);
            }
            catch (IllegalArgumentException var5) {
                throw new YAMLException("Could not deserialize object", var5);
            }
        }
        
        @Override
        public void construct2ndStep(final Node node, final Object object) {
            throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
        }
    }
}
