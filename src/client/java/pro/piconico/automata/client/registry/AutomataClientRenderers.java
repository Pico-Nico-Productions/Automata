package pro.piconico.automata.client.registry;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EntityRendererFactories;
import pro.piconico.automata.client.render.block.entity.LogisticChestBlockEntityRenderer;
import pro.piconico.automata.client.render.entity.ConstructionBotEntityRenderer;
import pro.piconico.automata.client.render.overlay.BlueprintRenderer;
import pro.piconico.automata.client.render.overlay.BotCacheRenderer;
import pro.piconico.automata.client.render.overlay.SelectionRenderer;
import pro.piconico.automata.registry.AutomataEntities;

public class AutomataClientRenderers {
    public static void initialize() {
        BlockEntityRendererFactories.register(AutomataEntities.LOGISTIC_CHEST, LogisticChestBlockEntityRenderer::new);

        EntityRendererFactories.register(AutomataEntities.CONSTRUCTION_BOT, ConstructionBotEntityRenderer::new);

        BlueprintRenderer.initialize();
        BotCacheRenderer.initialize();
		SelectionRenderer.initialize();
    }
}
