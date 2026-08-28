package pro.piconico.automata.client.registry;

import net.minecraft.util.Identifier;
import pro.piconico.automata.registry.AutomataRegistry;

public class AutomataClientRegistry {
    public static final String NO_CULL = "no_cull";

    public static Identifier pipelineId(String path) {
        return AutomataRegistry.id("pipeline/" + path);
    }
}
