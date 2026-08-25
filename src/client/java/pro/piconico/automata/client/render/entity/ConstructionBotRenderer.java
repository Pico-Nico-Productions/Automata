package pro.piconico.automata.client.render.entity;

import net.minecraft.client.render.entity.BeeEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.state.BeeEntityRenderState;
import net.minecraft.util.Identifier;
import pro.piconico.automata.registry.AutomataRegistry;

// TODO: Extend MobEntityRenderer instead and customize rendering
public class ConstructionBotRenderer extends BeeEntityRenderer {
    private static final Identifier TEXTURE = AutomataRegistry.entityTextureId(AutomataRegistry.CONSTRUCTION_BOT);

    public ConstructionBotRenderer(Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(BeeEntityRenderState beeEntityRenderState) {
        return TEXTURE;
    }
}
