package pro.piconico.automata.client.render.entity;

import net.minecraft.client.render.entity.BeeEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.state.BeeEntityRenderState;
import net.minecraft.util.Identifier;
import pro.piconico.automata.client.registry.AutomataClientTextures;

// TODO: Extend MobEntityRenderer instead and customize rendering
public class ConstructionBotEntityRenderer extends BeeEntityRenderer {
    public ConstructionBotEntityRenderer(Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(BeeEntityRenderState beeEntityRenderState) {
        return AutomataClientTextures.CONSTRUCTION_BOT.id();
    }
}
