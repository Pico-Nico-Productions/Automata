package pro.piconico.automata.client.network.handler;

import java.util.HashMap;
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
        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> staleEntry : payload.staleJobAssignments().entrySet()) {
            if (!BotCache.jobAssignments.containsKey(staleEntry.getKey()))
                continue;

            for (BotJobType<?> staleType : staleEntry.getValue().keySet()) {
                Map<BotJobType<?>, BotJobAssignment> typeMap = BotCache.jobAssignments.get(staleEntry.getKey());

                if (!typeMap.containsKey(staleType))
                    continue;

                if (typeMap.size() > 1) {
                    typeMap.remove(staleType);
                }
                else {
                    BotCache.jobAssignments.remove(staleEntry.getKey());
                    break;
                }
            }
        }

        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> newEntry : payload.newJobAssignments().entrySet()) {
            BotCache.jobAssignments.computeIfAbsent(newEntry.getKey(), blockPos -> new HashMap<>()).putAll(newEntry.getValue());
        }
    }
}
