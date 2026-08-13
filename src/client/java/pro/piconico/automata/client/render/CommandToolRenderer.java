package pro.piconico.automata.client.render;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.client.network.BotCache;
import pro.piconico.automata.component.CommandToolComponent;
import pro.piconico.automata.registry.AutomataBotJobs;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

public class CommandToolRenderer {
    private static final int NETWORK_ARGB = 0x60FFFF00; // Yellow
    private static final int DECONSTRUCTION_BOX_ARGB = 0xFFFF0000; // Red
    private static final int SELECTION_BOX_COLOR = 0xFF00FFFF; // Cyan
    private static final int SELECTION1_COLOR = 0xFF00FF00; // Green
    private static final int SELECTION2_COLOR = 0xFF0000FF; // Blue

    private static void renderNetworks(WorldRenderContext context) {
        for (ChunkPos chunkPos : BotCache.networks.keySet()) {
            RenderUtils.drawBox(context, ChunkBounds.of(chunkPos, 0, MinecraftClient.getInstance().world).toBox(), NETWORK_ARGB);
        }
    }

    private static void renderDeconstructionJobs(WorldRenderContext context) {
        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> entry : BotCache.jobAssignments.entrySet()) {
            if (!entry.getValue().containsKey(AutomataBotJobs.DECONSTRUCTION_JOB))
                continue;

            RenderUtils.drawBoxOutline(context, new Box(entry.getKey()), DECONSTRUCTION_BOX_ARGB);
        }
    }

    private static void renderSelectionOutline(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return;

        ItemStack stack = client.player.getMainHandStack();
        if (!stack.isOf(AutomataItems.COMMAND_TOOL)) {
            stack = client.player.getOffHandStack();
            if (!stack.isOf(AutomataItems.COMMAND_TOOL))
                return;
        }

        CommandToolComponent commandToolComponent = stack.getOrDefault(AutomataComponents.COMMAND_TOOL, CommandToolComponent.EMPTY);
        Optional<Box> selectionBox = commandToolComponent.getSelectionBox();
        if (selectionBox.isEmpty())
            return;

        RenderUtils.drawBoxOutline(context, selectionBox.get(), SELECTION_BOX_COLOR);
        RenderUtils.drawBoxOutline(context, new Box(commandToolComponent.selection1().get()), SELECTION1_COLOR);
        RenderUtils.drawBoxOutline(context, new Box(commandToolComponent.selection2().get()), SELECTION2_COLOR);
    }

    public static void initialize() {
        WorldRenderEvents.END_MAIN.register(CommandToolRenderer::renderNetworks);
        WorldRenderEvents.END_MAIN.register(CommandToolRenderer::renderDeconstructionJobs);
        WorldRenderEvents.END_MAIN.register(CommandToolRenderer::renderSelectionOutline);
    }
}
