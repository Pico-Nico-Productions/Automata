package pro.piconico.automata.client.render;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SequencedMap;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.client.design.AutomataColors;
import pro.piconico.automata.client.network.BotCache;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.entity.LivingEntityUtils;
import pro.piconico.automata.registry.AutomataBotJobs;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

public class AutomatoolRenderer {
    private static Optional<SelectionComponent> getSelectionComponent() {
        SequencedMap<Hand, ItemStack> stacks = LivingEntityUtils.getHeldStacks(MinecraftClient.getInstance().player, AutomataItems.AUTOMATOOL);

        if (stacks.isEmpty())
            return Optional.empty();

        return Optional.of(stacks.firstEntry().getValue().getOrDefault(AutomataComponents.SELECTION, SelectionComponent.EMPTY));
    }

    private static void renderNetworks(WorldRenderContext context) {
        for (ChunkPos chunkPos : BotCache.networkMap.keySet()) {
            RenderUtils.drawBox(context, ChunkBounds.of(chunkPos, 0, MinecraftClient.getInstance().world).toBox(), AutomataColors.NETWORK);
        }
    }

    private static void renderDeconstructionJobs(WorldRenderContext context) {
        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> entry : BotCache.jobAssignmentMap.entrySet()) {
            if (!entry.getValue().containsKey(AutomataBotJobs.DECONSTRUCTION))
                continue;

            RenderUtils.drawBoxOutline(context, new Box(entry.getKey()), AutomataColors.DECONSTRUCTION);
        }
    }

    private static void renderSelectionOutline(WorldRenderContext context) {
        Optional<SelectionComponent> selectionComponent = getSelectionComponent();

        selectionComponent.ifPresent(selection -> {
            if (selection.hasSelection()) RenderUtils.drawBoxOutline(context, selection.getSelectionBox().get(), AutomataColors.SELECTION_BOUNDS);

            if (selection.selection1().equals(selection.selection2()))
                return;

            selection.selection1().ifPresent(selection1 -> RenderUtils.drawBoxOutline(context, new Box(selection1), AutomataColors.SELECTION1));
            selection.selection2().ifPresent(selection2 -> RenderUtils.drawBoxOutline(context, new Box(selection2), AutomataColors.SELECTION2));
        });
    }

    public static void initialize() {
        WorldRenderEvents.END_MAIN.register(AutomatoolRenderer::renderNetworks);
        WorldRenderEvents.END_MAIN.register(AutomatoolRenderer::renderDeconstructionJobs);
        WorldRenderEvents.END_MAIN.register(AutomatoolRenderer::renderSelectionOutline);
    }
}
