package pro.piconico.automata.client.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.network.BotNetwork;

public class BotCache {
    public static final Map<ChunkPos, BotNetwork> networks = new HashMap<>();
    public static final Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> jobAssignments = new HashMap<>();

    public static void addNetwork(Entry<ChunkPos, BotNetwork> entry) {
        BotCache.networks.put(entry.getKey(), entry.getValue());
    }

    public static void removeNetwork(BotNetwork network) {
        for (ChunkPos chunkPos : network.getChunks()) {
            BotCache.networks.remove(chunkPos);
        }
    }

    public static void addJobAssignment(Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> entry) {
        BotCache.jobAssignments.computeIfAbsent(entry.getKey(), blockPos -> new HashMap<>()).putAll(entry.getValue());
    }

    public static void removeJobAssignment(Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> entry) {
        if (!BotCache.jobAssignments.containsKey(entry.getKey()))
            return;

        Map<BotJobType<?>, BotJobAssignment> typeMap = BotCache.jobAssignments.get(entry.getKey());
        for (BotJobType<?> type : entry.getValue().keySet()) {
            if (!typeMap.containsKey(type))
                continue;

            if (typeMap.size() > 1) {
                typeMap.remove(type);
            }
            else {
                BotCache.jobAssignments.remove(entry.getKey());
                break;
            }
        }
    }

    public static void clear() {
        networks.clear();
        jobAssignments.clear();
    }
}
