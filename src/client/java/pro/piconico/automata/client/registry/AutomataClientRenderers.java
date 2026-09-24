package pro.piconico.automata.client.registry;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EntityRendererFactories;
import pro.piconico.automata.client.render.AutomatoolRenderer;
import pro.piconico.automata.client.render.block.entity.LogisticChestBlockEntityRenderer;
import pro.piconico.automata.client.render.entity.ConstructionBotEntityRenderer;
import pro.piconico.automata.registry.AutomataEntities;

public class AutomataClientRenderers {
    public static void initialize() {
        EntityRendererFactories.register(AutomataEntities.CONSTRUCTION_BOT, ConstructionBotEntityRenderer::new);
        BlockEntityRendererFactories.register(AutomataEntities.LOGISTIC_CHEST, LogisticChestBlockEntityRenderer::new);

		AutomatoolRenderer.initialize();
    }
}
