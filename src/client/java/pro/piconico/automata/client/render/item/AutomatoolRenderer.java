package pro.piconico.automata.client.render.item;

import java.util.Map;
import io.wispforest.owo.ui.core.Color;
import java.util.Optional;
import java.util.SequencedMap;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.network.BotNetwork;
import pro.piconico.automata.client.network.BotCache;
import pro.piconico.automata.client.registry.AutomataClientColors;
import pro.piconico.automata.client.render.RenderUtils;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.entity.LivingEntityUtils;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

public class AutomatoolRenderer {
    private static Optional<SelectionComponent> getSelectionComponent() {
        SequencedMap<Hand, ItemStack> stacks = LivingEntityUtils.getHeldStacks(MinecraftClient.getInstance().player, AutomataItems.AUTOMATOOL);

        if (stacks.isEmpty())
            return Optional.empty();

        return SelectionComponent.get(stacks.firstEntry().getValue());
    }

    private static void renderNetworks(WorldRenderContext context) {
        for (ChunkPos chunkPos : BotCache.networkMap.keySet()) {
            RenderUtils.drawBox(context, ChunkBounds.of(chunkPos, 0, MinecraftClient.getInstance().world).toBox(), AutomataClientColors.NETWORK);
        }

        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
            return;

        for (BotNetwork network : BotCache.networkMap.values()) {
            for (BlockPos pos : network.getRoboports()) {
                RenderUtils.drawBoxOutline(context, new Box(pos), AutomataClientColors.NETWORK);
            }

            for (BlockPos pos : network.getLogisticStorages()) {
                RenderUtils.drawBoxOutline(context, new Box(pos), AutomataClientColors.NETWORK);
            }
        }
    }

    private static void renderJobs(WorldRenderContext context) {
        for (Map<BotJobType<?>,BotJobAssignment> typeMap : BotCache.jobAssignmentMap.values()) {
            for (BotJobAssignment jobAssignment : typeMap.values()) {
                Optional<Color> jobColor = AutomataClientColors.getJobColor(jobAssignment.JOB);

                if (jobColor.isEmpty())
                    continue;

                RenderUtils.drawBoxOutline(context, new Box(jobAssignment.JOB.pos()), AutomataClientColors.DECONSTRUCTION);
            }
        }
    }

    private static void renderSelectionOutline(WorldRenderContext context) {
        Optional<SelectionComponent> selectionComponent = getSelectionComponent();

        selectionComponent.ifPresent(selection -> {
            if (!MinecraftClient.getInstance().world.getRegistryKey().getValue().equals(selection.worldId()))
                return;

            selection.getSelectionBox().ifPresent(box -> RenderUtils.drawBoxOutline(context, box, AutomataClientColors.SELECTION_BOUNDS));

            if (selection.selection1().equals(selection.selection2()))
                return;

            selection.selection1().ifPresent(selection1 -> RenderUtils.drawBoxOutline(context, new Box(selection1), AutomataClientColors.SELECTION1));
            selection.selection2().ifPresent(selection2 -> RenderUtils.drawBoxOutline(context, new Box(selection2), AutomataClientColors.SELECTION2));
        });
    }

    public static void initialize() {
        WorldRenderEvents.END_MAIN.register(AutomatoolRenderer::renderNetworks);
        WorldRenderEvents.END_MAIN.register(AutomatoolRenderer::renderJobs);
        WorldRenderEvents.END_MAIN.register(AutomatoolRenderer::renderSelectionOutline);
    }
}
