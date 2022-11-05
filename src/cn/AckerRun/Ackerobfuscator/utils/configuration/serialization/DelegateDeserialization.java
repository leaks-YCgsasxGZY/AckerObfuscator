package cn.AckerRun.Ackerobfuscator.utils.configuration.serialization;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface DelegateDeserialization {
    Class<? extends ConfigurationSerializable> value();
}
