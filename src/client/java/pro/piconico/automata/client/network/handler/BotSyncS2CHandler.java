package pro.piconico.automata.client.network.handler;

import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.client.network.BotCache;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;

public class BotSyncS2CHandler {
    public static void handle(BotSyncS2CPacket payload, ClientPlayNetworking.Context context) {
        if (payload.clear()) {
            BotCache.clear();
        }
        else {
            for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> staleEntry : payload.staleJobAssignments().entrySet()) {
                BotCache.remove(staleEntry);
            }
        }

        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> newEntry : payload.newJobAssignments().entrySet()) {
            BotCache.add(newEntry);
        }
    }
}
