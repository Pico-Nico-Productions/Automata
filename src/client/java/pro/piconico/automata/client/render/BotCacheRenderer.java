package pro.piconico.automata.client.render;

import java.util.Map;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.network.BotNetwork;
import pro.piconico.automata.client.network.BotCache;
import pro.piconico.automata.client.registry.AutomataClientColors;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

public class BotCacheRenderer {
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
        for (Map<BotJobType<?>, BotJobAssignment> typeMap : BotCache.jobAssignmentMap.values()) {
            for (BotJobAssignment jobAssignment : typeMap.values()) {
                Color jobColor = AutomataClientColors.getJobColor(jobAssignment.job);
                RenderUtils.drawBoxOutline(context, new Box(jobAssignment.job.pos()), jobColor);
            }
        }
    }

    public static void initialize() {
        WorldRenderEvents.END_MAIN.register(BotCacheRenderer::renderNetworks);
        WorldRenderEvents.END_MAIN.register(BotCacheRenderer::renderJobs);
    }
}
