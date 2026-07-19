package pro.piconico.automata.client.render;

import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
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
    private static double EXPAND = 0.002;
    private static int SELECTION_BOX_COLOR = 0xFFFF0000; // Red
    private static int SELECTION1_COLOR = 0xFF00FF00; // Green
    private static int SELECTION2_COLOR = 0xFF0000FF; // Blue
    private static float OUTLINE_WIDTH = 4.0f;

    private static void renderSelectionBox(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ItemStack stack = client.player.getMainHandStack();
        if (!stack.isOf(AutomataItems.COMMAND_TOOL)) return;

        CommandToolComponent commandToolComponent = stack.getOrDefault(AutomataComponents.COMMAND_TOOL, CommandToolComponent.EMPTY);
        Optional<Box> selectionBox = commandToolComponent.getSelectionBox();
        if (selectionBox.isEmpty()) return;
        
        Vec3d cameraPos = context.gameRenderer().getCamera().getCameraPos();
        VoxelShape selectionBoxShape = VoxelShapes.cuboid(selectionBox.get().expand(EXPAND));
        VertexRendering.drawOutline(context.matrices(), context.consumers().getBuffer(RenderLayers.SECONDARY_BLOCK_OUTLINE), selectionBoxShape, -cameraPos.x, -cameraPos.y, -cameraPos.z, SELECTION_BOX_COLOR, OUTLINE_WIDTH);
        VoxelShape selection1Shape = VoxelShapes.cuboid(new Box(commandToolComponent.selection1().get()).expand(EXPAND));
        VertexRendering.drawOutline(context.matrices(), context.consumers().getBuffer(RenderLayers.SECONDARY_BLOCK_OUTLINE), selection1Shape, -cameraPos.x, -cameraPos.y, -cameraPos.z, SELECTION1_COLOR, OUTLINE_WIDTH);
        VoxelShape selection2Shape = VoxelShapes.cuboid(new Box(commandToolComponent.selection2().get()).expand(EXPAND));
        VertexRendering.drawOutline(context.matrices(), context.consumers().getBuffer(RenderLayers.SECONDARY_BLOCK_OUTLINE), selection2Shape, -cameraPos.x, -cameraPos.y, -cameraPos.z, SELECTION2_COLOR, OUTLINE_WIDTH);
    }

    public static void initialize() {
        WorldRenderEvents.END_MAIN.register(CommandToolRenderer::renderSelectionBox);
    }
}
