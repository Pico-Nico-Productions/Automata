package pro.piconico.automata.client.render.entity;

import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BeeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BeeEntityRenderState;
import net.minecraft.util.Identifier;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.entity.ConstructionBotEntity;

// TODO: Create custom render state and model
public class ConstructionBotEntityRenderer extends MobEntityRenderer<ConstructionBotEntity, BeeEntityRenderState, BeeEntityModel> {
    public static float SHADOW_RADIUS = 0.4F;

    public ConstructionBotEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new BeeEntityModel(context.getPart(EntityModelLayers.BEE)), SHADOW_RADIUS);
    }

    @Override
    public BeeEntityRenderState createRenderState() {
        BeeEntityRenderState beeRenderState = new BeeEntityRenderState();
        beeRenderState.hasStinger = false;
        return beeRenderState;
    }

    @Override
    public Identifier getTexture(BeeEntityRenderState beeEntityRenderState) {
        return AutomataClientTextures.CONSTRUCTION_BOT.id();
    }
}
