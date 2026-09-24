package pro.piconico.automata.client.render.block.entity;

import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.client.render.block.entity.model.ChestBlockModel;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.ChestBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import org.jspecify.annotations.Nullable;
import pro.piconico.automata.block.LogisticChestBlock;
import pro.piconico.automata.block.entity.LogisticChestBlockEntity;
import pro.piconico.automata.registry.AutomataRegistry;

public class LogisticChestBlockEntityRenderer implements BlockEntityRenderer<LogisticChestBlockEntity, ChestBlockEntityRenderState> {
    private final ChestBlockModel model;
    private final SpriteHolder spriteHolder;
    private final SpriteIdentifier spriteIdentifier = TexturedRenderLayers.CHEST_SPRITE_MAPPER.map(AutomataRegistry.id(AutomataRegistry.LOGISTIC_CHEST));

    public LogisticChestBlockEntityRenderer(Context context) {
        model = new ChestBlockModel(context.getLayerModelPart(EntityModelLayers.CHEST));
        spriteHolder = context.spriteHolder();
    }

    @Override
    public ChestBlockEntityRenderState createRenderState() {
        return new ChestBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(LogisticChestBlockEntity blockEntity, ChestBlockEntityRenderState state, float tickProgress, Vec3d cameraPos,
            @Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderState.updateBlockEntityRenderState(blockEntity, state, crumblingOverlay);

        state.chestType = ChestType.SINGLE;

        state.yaw = blockEntity.getCachedState().get(LogisticChestBlock.FACING).getPositiveHorizontalDegrees();

        state.lidAnimationProgress = blockEntity.getAnimationProgress(tickProgress);
    }

    @Override
    public void render(ChestBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        float openness = state.lidAnimationProgress;
        openness = 1.0F - openness;
        openness = 1.0F - openness * openness * openness;

        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-state.yaw));
        matrices.translate(-0.5F, -0.5F, -0.5F);

        RenderLayer renderLayer = spriteIdentifier.getRenderLayer(RenderLayers::entityCutout);

        Sprite sprite = spriteHolder.getSprite(spriteIdentifier);

        queue.submitModel(model, openness, matrices, renderLayer, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, -1, sprite, 0, state.crumblingOverlay);

        matrices.pop();
    }
}
