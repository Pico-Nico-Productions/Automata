package pro.piconico.automata.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import pro.piconico.automata.Automata;

public class AutomataRegistry {
    public static final String BOT_PERSISTENT_STATE = "bot_persistent_state";
    public static final String COMMAND_TOOL = "command_tool";
    public static final String CONSTRUCTION_BOT = "construction_bot";
    public static final String ROBOPORT = "roboport";

    public static Identifier id(String path) {
        return Identifier.of(Automata.MOD_ID, path);
    }

    public static Identifier packetId(String path) {
        return id("packet/" + path);
    }

    public static Identifier textureId(String path) {
        return id("textures/" + path + ".png");
    }

    public static Identifier guiTextureId(String path) {
        return textureId("gui/" + path);
    }

    public static <T> RegistryKey<T> toRegistryKey(RegistryKey<Registry<T>> registryKey, String path) {
        return RegistryKey.of(registryKey, id(path));
    }
}
