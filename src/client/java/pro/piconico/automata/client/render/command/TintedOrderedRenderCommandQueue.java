package pro.piconico.automata.client.render.command;

import java.util.List;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState.LeashData;
import net.minecraft.client.render.entity.state.EntityRenderState.ShadowPiece;
import net.minecraft.client.render.item.ItemRenderState.Glint;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class TintedOrderedRenderCommandQueue implements OrderedRenderCommandQueue {
    private final OrderedRenderCommandQueue orderedRenderCommandQueue;
    private final int tintArgb;

    public TintedOrderedRenderCommandQueue(OrderedRenderCommandQueue orderedRenderCommandQueue, int tintArgb) {
        this.orderedRenderCommandQueue = orderedRenderCommandQueue;
        this.tintArgb = tintArgb;
    }

    private int tintColor(int colorArgb) {
        int a = ((colorArgb >>> 24) & 0xFF) * ((tintArgb >>> 24) & 0xFF) / 255;
        int r = ((colorArgb >>> 16) & 0xFF) * ((tintArgb >>> 16) & 0xFF) / 255;
        int g = ((colorArgb >>> 8) & 0xFF) * ((tintArgb >>> 8) & 0xFF) / 255;
        int b = (colorArgb & 0xFF) * (tintArgb & 0xFF) / 255;

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void submitBlock(MatrixStack matrices, BlockState state, int light, int overlay, int outlineColor) {
        orderedRenderCommandQueue.submitBlock(matrices, state, light, overlay, outlineColor);
    }

    @Override
    public void submitBlockStateModel(MatrixStack matrices, RenderLayer renderLayer, BlockStateModel model, float r, float g, float b, int light, int overlay,
            int outlineColor) {
        orderedRenderCommandQueue.submitBlockStateModel(matrices, renderLayer, model, r, g, b, light, overlay, outlineColor);
    }

    @Override
    public void submitCustom(LayeredCustom customRenderer) {
        orderedRenderCommandQueue.submitCustom(customRenderer);
    }

    @Override
    public void submitCustom(MatrixStack matrices, RenderLayer renderLayer, Custom customRenderer) {
        orderedRenderCommandQueue.submitCustom(matrices, renderLayer, customRenderer);
    }

    @Override
    public void submitFire(MatrixStack matrices, EntityRenderState renderState, Quaternionf rotation) {
        orderedRenderCommandQueue.submitFire(matrices, renderState, rotation);
    }

    @Override
    public void submitItem(MatrixStack matrices, ItemDisplayContext displayContext, int light, int overlay, int outlineColors, int[] tintLayers,
            List<BakedQuad> quads, RenderLayer renderLayer, Glint glintType) {
        orderedRenderCommandQueue.submitItem(matrices, displayContext, light, overlay, outlineColors, tintLayers, quads, renderLayer, glintType);
    }

    @Override
    public void submitLabel(MatrixStack arg0, @Nullable Vec3d arg1, int arg2, Text arg3, boolean arg4, int arg5, double arg6, CameraRenderState arg7) {
        orderedRenderCommandQueue.submitLabel(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    @Override
    public void submitLeash(MatrixStack matrices, LeashData leashData) {
        submitLeash(matrices, leashData);
    }

    @Override
    public <S> void submitModel(Model<? super S> model, S state, MatrixStack matrices, RenderLayer renderLayer, int light, int overlay, int tintedColor,
            Sprite sprite, int outlineColor, ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        orderedRenderCommandQueue.submitModel(model, state, matrices, renderLayer, light, overlay, tintColor(tintedColor), sprite, outlineColor,
                crumblingOverlay);
    }

    @Override
    public void submitModelPart(ModelPart arg0, MatrixStack arg1, RenderLayer arg2, int arg3, int arg4, @Nullable Sprite arg5, boolean arg6, boolean arg7,
            int arg8, @Nullable CrumblingOverlayCommand arg9, int arg10) {
        orderedRenderCommandQueue.submitModelPart(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
    }

    @Override
    public void submitMovingBlock(MatrixStack matrices, MovingBlockRenderState state) {
        orderedRenderCommandQueue.submitMovingBlock(matrices, state);
    }

    @Override
    public void submitShadowPieces(MatrixStack matrices, float shadowRadius, List<ShadowPiece> shadowPieces) {
        orderedRenderCommandQueue.submitShadowPieces(matrices, shadowRadius, shadowPieces);
    }

    @Override
    public void submitText(MatrixStack matrices, float x, float y, OrderedText text, boolean dropShadow, TextLayerType layerType, int light, int color,
            int backgroundColor, int outlineColor) {
        orderedRenderCommandQueue.submitText(matrices, x, y, text, dropShadow, layerType, light, color, backgroundColor, outlineColor);
    }

    @Override
    public RenderCommandQueue getBatchingQueue(int order) {
        return new TintedOrderedRenderCommandQueue((OrderedRenderCommandQueue)orderedRenderCommandQueue.getBatchingQueue(order), tintArgb);
    }
}
