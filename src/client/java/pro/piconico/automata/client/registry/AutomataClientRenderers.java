package pro.piconico.automata.client.registry;

import net.minecraft.client.render.entity.EntityRendererFactories;
import pro.piconico.automata.client.render.CommandToolRenderer;
import pro.piconico.automata.client.render.entity.ConstructionBotRenderer;
import pro.piconico.automata.registry.AutomataEntities;

public class AutomataClientRenderers {
    public static void initialize() {
        EntityRendererFactories.register(AutomataEntities.CONSTRUCTION_BOT, ConstructionBotRenderer::new);
        
		CommandToolRenderer.initialize();
    }
}
