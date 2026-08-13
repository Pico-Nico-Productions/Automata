package pro.piconico.automata.client.render;

import org.joml.Vector3f;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class RenderUtils {
    private static final int[] BOX_QUAD_INDEXES = { //
            0, 1, 2, 3, // Bottom
            4, 7, 6, 5, // Top
            0, 4, 5, 1, // North
            2, 6, 7, 3, // South
            0, 3, 7, 4, // West
            1, 5, 6, 2, // East
    };
    private static final float OUTLINE_WIDTH = 4.0f;

    private static Vector3f[] boxToVertices(Box box) {
        float minX = (float)box.minX, minY = (float)box.minY, minZ = (float)box.minZ;
        float maxX = (float)box.maxX, maxY = (float)box.maxY, maxZ = (float)box.maxZ;
        return new Vector3f[] { //
                new Vector3f(minX, minY, minZ), // Bottom
                new Vector3f(maxX, minY, minZ), //
                new Vector3f(maxX, minY, maxZ), //
                new Vector3f(minX, minY, maxZ), //
                new Vector3f(minX, maxY, minZ), // Top
                new Vector3f(maxX, maxY, minZ), //
                new Vector3f(maxX, maxY, maxZ), //
                new Vector3f(minX, maxY, maxZ), //
        };
    }

    public static void drawBoxOutline(WorldRenderContext context, Box box, int argbColor) {
        Vec3d cameraPos = context.gameRenderer().getCamera().getCameraPos();
        VoxelShape boxShape = VoxelShapes.cuboid(box);
        VertexRendering.drawOutline(context.matrices(), context.consumers().getBuffer(RenderLayers.SECONDARY_BLOCK_OUTLINE), boxShape, -cameraPos.x,
                -cameraPos.y, -cameraPos.z, argbColor, OUTLINE_WIDTH);
    }

    public static void drawBox(WorldRenderContext context, Box box, int argbColor) {
        VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayers.debugFilledBox());
        MatrixStack.Entry matrix = context.matrices().peek();
        Vector3f[] boxVertices = boxToVertices(box.offset(context.gameRenderer().getCamera().getCameraPos().negate()));
        for (int index : BOX_QUAD_INDEXES) {
            vertexConsumer.vertex(matrix, boxVertices[index]).color(argbColor);
        }
    }
}
