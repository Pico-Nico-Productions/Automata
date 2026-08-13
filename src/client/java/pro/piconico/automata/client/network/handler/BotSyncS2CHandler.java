package pro.piconico.automata.client.network.handler;

import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.network.BotNetwork;
import pro.piconico.automata.client.network.BotCache;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;

public class BotSyncS2CHandler {
    public static void handle(BotSyncS2CPacket payload, ClientPlayNetworking.Context context) {
        if (payload.clear()) {
            BotCache.clear();
        }
        else {
            for (BotNetwork obsoleteNetwork : payload.obsoleteNetworks().values().stream().distinct().toList()) {
                BotCache.removeNetwork(obsoleteNetwork);
            }
            for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> obsoleteEntry : payload.obsoleteJobAssignments().entrySet()) {
                BotCache.removeJobAssignment(obsoleteEntry);
            }
        }

        for (Entry<ChunkPos, BotNetwork> mutatedEntry : payload.mutatedNetworks().entrySet()) {
            BotCache.addNetwork(mutatedEntry);
        }
        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> newEntry : payload.mutatedJobAssignments().entrySet()) {
            BotCache.addJobAssignment(newEntry);
        }
    }
}
