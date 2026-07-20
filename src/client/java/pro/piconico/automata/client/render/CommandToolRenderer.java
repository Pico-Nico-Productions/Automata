package pro.piconico.automata.client.render;

import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import pro.piconico.automata.component.CommandToolComponent;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataItems;

public class CommandToolRenderer {
    private static final double EXPAND = 0.002;
    private static final RenderLayer OUTLINE_LAYER = RenderLayers.SECONDARY_BLOCK_OUTLINE;
    private static final float OUTLINE_WIDTH = 4.0f;
    private static final int SELECTION_BOX_COLOR = 0xFFFF0000; // Red
    private static final int SELECTION1_COLOR = 0xFF00FF00; // Green
    private static final int SELECTION2_COLOR = 0xFF0000FF; // Blue

    private static void renderBoxOutline(WorldRenderContext context, Box box, int color) {
        Vec3d cameraPos = context.gameRenderer().getCamera().getCameraPos();
        VoxelShape boxShape = VoxelShapes.cuboid(box.expand(EXPAND));
        VertexRendering.drawOutline(context.matrices(), context.consumers().getBuffer(OUTLINE_LAYER), boxShape, -cameraPos.x, -cameraPos.y, -cameraPos.z, color, OUTLINE_WIDTH);
    }

    private static void renderSelectionOutline(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ItemStack stack = client.player.getMainHandStack();
        if (!stack.isOf(AutomataItems.COMMAND_TOOL)) return;

        CommandToolComponent commandToolComponent = stack.getOrDefault(AutomataComponents.COMMAND_TOOL, CommandToolComponent.EMPTY);
        Optional<Box> selectionBox = commandToolComponent.getSelectionBox();
        if (selectionBox.isEmpty()) return;
        
        renderBoxOutline(context, selectionBox.get(), SELECTION_BOX_COLOR);
        renderBoxOutline(context, new Box(commandToolComponent.selection1().get()), SELECTION1_COLOR);
        renderBoxOutline(context, new Box(commandToolComponent.selection2().get()), SELECTION2_COLOR);
    }

    public static void initialize() {
        WorldRenderEvents.END_MAIN.register(CommandToolRenderer::renderSelectionOutline);
    }
}
