package pro.piconico.automata.registry;

import net.minecraft.registry.RegistryKey;

public class RegistryUtils {
    public static boolean keysEqual(RegistryKey<?> key1, RegistryKey<?> key2) {
        return key1.getRegistry().equals(key2.getRegistry()) && key1.getValue().equals(key2.getValue());
    }
}
