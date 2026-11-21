package pro.piconico.automata.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayers;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LayeringTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import pro.piconico.automata.client.registry.AutomataClientColors;
import pro.piconico.automata.client.registry.AutomataClientRenderPipelines;
import pro.piconico.automata.client.render.command.TintedOrderedRenderCommandQueue;

public class RenderUtils {
    public static final RenderLayer TRANSLUCENT_NO_CULL = RenderLayer.of("translucent_no_cull",
            RenderSetup.builder(AutomataClientRenderPipelines.NO_CULL).translucent().layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).build());
    private static final int[] BOX_QUAD_INDEXES = { //
            0, 1, 2, 3, // Bottom
            4, 7, 6, 5, // Top
            0, 4, 5, 1, // North
            2, 6, 7, 3, // South
            0, 3, 7, 4, // West
            1, 5, 6, 2, // East
    };
    private static final float OUTLINE_WIDTH = 4.0f;
    private static final float HALF_BLOCK = .5F;

    private static Vector3f[] boxToVertices(Box box) {
        float minX = (float)box.minX, minY = (float)box.minY, minZ = (float)box.minZ;
        float maxX = (float)box.maxX, maxY = (float)box.maxY, maxZ = (float)box.maxZ;
        return new Vector3f[] { //
                new Vector3f(minX, minY, minZ), new Vector3f(maxX, minY, minZ), new Vector3f(maxX, minY, maxZ), new Vector3f(minX, minY, maxZ), // Bottom
                new Vector3f(minX, maxY, minZ), new Vector3f(maxX, maxY, minZ), new Vector3f(maxX, maxY, maxZ), new Vector3f(minX, maxY, maxZ), // Top
        };
    }

    public static void drawBoxOutline(WorldRenderContext context, Box box, Color color) {
        Vec3d cameraPos = context.gameRenderer().getCamera().getCameraPos();
        VoxelShape boxShape = VoxelShapes.cuboid(box);
        VertexRendering.drawOutline(context.matrices(), context.consumers().getBuffer(RenderLayers.SECONDARY_BLOCK_OUTLINE), boxShape, -cameraPos.x,
                -cameraPos.y, -cameraPos.z, color.argb(), OUTLINE_WIDTH);
    }

    public static void drawBox(WorldRenderContext context, RenderLayer layer, Box box, Color color) {
        VertexConsumer vertexConsumer = context.consumers().getBuffer(layer);
        MatrixStack.Entry matrix = context.matrices().peek();
        Vector3f[] boxVertices = boxToVertices(box.offset(context.gameRenderer().getCamera().getCameraPos().negate()));
        for (int index : BOX_QUAD_INDEXES) {
            vertexConsumer.vertex(matrix, boxVertices[index]).color(color.argb());
        }
    }

    public static void drawBox(WorldRenderContext context, Box box, Color color) {
        drawBox(context, TRANSLUCENT_NO_CULL, box, color);
    }

    private static void drawBlock(WorldRenderContext context, MatrixStack matrices, BlockPos worldPos, BlockState state, Color tint) {
        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumer consumer = new TintedVertexConsumer(context.consumers().getBuffer(BlockRenderLayers.getMovingBlockLayer(state)), tint.argb());
        Random random = Random.create(state.getRenderingSeed(worldPos));
        List<BlockModelPart> parts = new ArrayList<>();
        client.getBlockRenderManager().getModel(state).addParts(random, parts);
        client.getBlockRenderManager().renderBlock(state, worldPos, client.world, matrices, consumer, false, parts);
    }

    private static void drawBlockEntity(WorldRenderContext context, MatrixStack matrices, BlockPos worldPos, BlockState state, BlockEntity blockEntity,
            Color tint) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (blockEntity == null) {
            drawBlock(context, matrices, worldPos, state, tint);
            return;
        }

        blockEntity.setWorld(client.world);

        float tickProgress = client.getRenderTickCounter().getTickProgress(true);
        BlockEntityRenderState renderState = client.getBlockEntityRenderDispatcher().getRenderState(blockEntity, tickProgress, null);

        if (renderState == null) {
            drawBlock(context, matrices, worldPos, state, tint);
            return;
        }

        TintedOrderedRenderCommandQueue tintedOrderedRenderCommandQueue = new TintedOrderedRenderCommandQueue(context.commandQueue(),
                AutomataClientColors.BLUEPRINT.argb());
        CameraRenderState cameraRenderState = new CameraRenderState();
        Camera camera = context.gameRenderer().getCamera();
        cameraRenderState.pos = camera.getCameraPos();
        cameraRenderState.blockPos = camera.getBlockPos();
        cameraRenderState.orientation = camera.getRotation();
        cameraRenderState.initialized = true;
        client.getBlockEntityRenderDispatcher().render(renderState, matrices, tintedOrderedRenderCommandQueue, cameraRenderState);
    }

    // TODO: Resolve transparency cutting through other drawn blocks
    public static void drawBlock(WorldRenderContext context, BlockPos worldPos, Quaternionfc rotation, BlockState state, Optional<BlockEntity> blockEntity,
            Color tint) {
        MatrixStack matrices = context.matrices();
        Vec3d cameraPos = context.gameRenderer().getCamera().getCameraPos();

        matrices.push();
        matrices.translate(worldPos.getX() - cameraPos.getX(), worldPos.getY() - cameraPos.getY(), worldPos.getZ() - cameraPos.getZ());

        matrices.translate(HALF_BLOCK, HALF_BLOCK, HALF_BLOCK);
        matrices.multiply(rotation);
        matrices.translate(-HALF_BLOCK, -HALF_BLOCK, -HALF_BLOCK);

        if (blockEntity.isPresent()) {
            drawBlockEntity(context, matrices, worldPos, state, blockEntity.get(), tint);
        }
        else {
            drawBlock(context, matrices, worldPos, state, tint);
        }

        matrices.pop();
    }

    public static void drawBlock(WorldRenderContext context, BlockPos worldPos, Quaternionfc rotation, BlockState state, Optional<BlockEntity> blockEntity) {
        drawBlock(context, worldPos, rotation, state, blockEntity, Color.WHITE);
    }
}
