package pro.piconico.automata.client.render;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.UUID;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.client.network.BotCache;
import pro.piconico.automata.component.CommandToolComponent;
import pro.piconico.automata.entity.LivingEntityUtils;
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

    private static Optional<CommandToolComponent> getCommandToolComponent() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        SequencedSet<Hand> hands = LivingEntityUtils.getHandsHolding(player, AutomataItems.COMMAND_TOOL);

        if (hands.isEmpty())
            return Optional.empty();

        ItemStack stack = player.getStackInHand(hands.getFirst());
        return Optional.of(stack.getOrDefault(AutomataComponents.COMMAND_TOOL, CommandToolComponent.EMPTY));
    }

    private static void renderNetworks(WorldRenderContext context) {
        for (ChunkPos chunkPos : BotCache.networkMap.keySet()) {
            RenderUtils.drawBox(context, ChunkBounds.of(chunkPos, 0, MinecraftClient.getInstance().world).toBox(), NETWORK_ARGB);
        }
    }

    private static void renderDeconstructionJobs(WorldRenderContext context) {
        Optional<CommandToolComponent> commandToolComponent = getCommandToolComponent();

        if (commandToolComponent.isEmpty() || commandToolComponent.get().teamUuid().isEmpty())
            return;

        UUID teamUuid = commandToolComponent.get().teamUuid().get();

        if (!BotCache.jobAssignmentMap.containsKey(teamUuid))
            return;

        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> entry : BotCache.jobAssignmentMap.get(teamUuid).entrySet()) {
            if (!entry.getValue().containsKey(AutomataBotJobs.DECONSTRUCTION_JOB))
                continue;

            RenderUtils.drawBoxOutline(context, new Box(entry.getKey()), DECONSTRUCTION_BOX_ARGB);
        }
    }

    private static void renderSelectionOutline(WorldRenderContext context) {
        Optional<CommandToolComponent> commandToolComponent = getCommandToolComponent();

        if (commandToolComponent.isEmpty() || !commandToolComponent.get().hasSelection())
            return;

        RenderUtils.drawBoxOutline(context, commandToolComponent.get().getSelectionBox().get(), SELECTION_BOX_COLOR);
        RenderUtils.drawBoxOutline(context, new Box(commandToolComponent.get().selection1().get()), SELECTION1_COLOR);
        RenderUtils.drawBoxOutline(context, new Box(commandToolComponent.get().selection2().get()), SELECTION2_COLOR);
    }

    public static void initialize() {
        WorldRenderEvents.END_MAIN.register(CommandToolRenderer::renderNetworks);
        WorldRenderEvents.END_MAIN.register(CommandToolRenderer::renderDeconstructionJobs);
        WorldRenderEvents.END_MAIN.register(CommandToolRenderer::renderSelectionOutline);
    }
}
