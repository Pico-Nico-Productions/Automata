package pro.piconico.automata.client.registry;

import net.minecraft.util.Identifier;
import pro.piconico.automata.registry.AutomataRegistry;

public class AutomataClientRegistry {
    public static final String NO_CULL = "no_cull";
    public static final String BOT_DEVICES = "bot_devices";

    public static Identifier pipelineId(String path) {
        return AutomataRegistry.id("pipeline/" + path);
    }

    public static Identifier textureId(String path) {
        return AutomataRegistry.id("textures/" + path + ".png");
    }

    public static Identifier entityTextureId(String path) {
        return textureId("entity/" + path);
    }

    public static Identifier guiTextureId(String path) {
        return textureId("gui/" + path);
    }

    public static Identifier screenGuiTextureId(String path) {
        return guiTextureId("screen/" + path);
    }
}
