package cn.AckerRun.Ackerobfuscator.utils.samples;

import java.lang.invoke.*;

public class DynamicInvoke
{
    public static Object invoke(final Object... v) {
        try {
            v[3] = ((MethodHandle)v[3]).invoke();
            v[2] = ((MethodHandle)v[2]).invoke();
            return ((MethodHandle)v[4]).bindTo(((MethodHandle)v[5]).invokeWithArguments(MethodHandles.publicLookup(), v[1], v[2], MethodType.fromMethodDescriptorString(v[3].toString(), ((Class)v[1]).getClassLoader()))).invoke(v[0]);
        }
        catch (Throwable $ex) {
             throw new RuntimeException("DynamicInvoke");
        }
    }
}
