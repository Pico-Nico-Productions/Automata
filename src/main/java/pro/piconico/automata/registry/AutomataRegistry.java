package pro.piconico.automata.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import pro.piconico.automata.Automata;

public class AutomataRegistry {
    public static <T> RegistryKey<T> toRegistryKey(RegistryKey<Registry<T>> registryKey, String path) {
        return RegistryKey.of(registryKey, Identifier.of(Automata.MOD_ID, path));
    }
}
