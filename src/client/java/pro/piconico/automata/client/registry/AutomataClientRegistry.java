package pro.piconico.automata.client.registry;

import net.minecraft.util.Identifier;
import pro.piconico.automata.registry.AutomataRegistry;

public class AutomataClientRegistry {
    public static final String PIPELINE_FOLDER = "pipeline/";
    public static final String TEXTURES_FOLDER = "textures/";
    public static final String ENTITY_TEXTURES_FOLDER = TEXTURES_FOLDER + "entity/";
    public static final String GUI_TEXTURES_FOLDER = TEXTURES_FOLDER + "gui/";
    public static final String SCREEN_GUI_TEXTURES_FOLDER = GUI_TEXTURES_FOLDER + "screen/";
    public static final String TEXTURE_FILE_EXTENSION = ".png";

    public static final String NO_CULL = "no_cull";
    public static final String BOT_DEVICE_ICONS = AutomataRegistry.BOT_DEVICE + "_icons";

    public static Identifier pipelineId(String path) {
        return AutomataRegistry.id(PIPELINE_FOLDER + path);
    }

    public static Identifier entityTextureId(String path) {
        return AutomataRegistry.id(ENTITY_TEXTURES_FOLDER + path + TEXTURE_FILE_EXTENSION);
    }

    public static Identifier screenGuiTextureId(String path) {
        return AutomataRegistry.id(SCREEN_GUI_TEXTURES_FOLDER + path + TEXTURE_FILE_EXTENSION);
    }
}
