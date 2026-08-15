package pro.piconico.automata.client.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobAssignmentMap;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.network.BotNetwork;
import pro.piconico.automata.bot.network.BotNetworkMap;

public class BotCache {
    public static final BotNetworkMap<BotNetwork> networkMap = new BotNetworkMap<>();
    public static final BotJobAssignmentMap jobAssignmentMap = new BotJobAssignmentMap();

    public static void applyDelta(BotNetworkMap<?> deltaMap) {
        for (Entry<ChunkPos, ?> networkEntry : deltaMap.entrySet()) {
            ChunkPos chunkPos = networkEntry.getKey();
            Object network = networkEntry.getValue();

            if (network == null) {
                networkMap.remove(chunkPos);
                continue;
            }

            networkMap.put(chunkPos, (BotNetwork)network);
        }
    }

    public static void applyDelta(BotJobAssignmentMap deltaMap) {
        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> jobAssignmentEntry : deltaMap.entrySet()) {
            BlockPos blockPos = jobAssignmentEntry.getKey();
            Map<BotJobType<?>, BotJobAssignment> typeMap = jobAssignmentEntry.getValue();

            if (typeMap == null) {
                jobAssignmentMap.remove(blockPos);
                continue;
            }
            
            Map<BotJobType<?>, BotJobAssignment> cacheTypeMap = jobAssignmentMap.computeIfAbsent(blockPos, pos -> new HashMap<>());
            
            for (Entry<BotJobType<?>, BotJobAssignment> typeEntry : typeMap.entrySet()) {
                BotJobType<?> type = typeEntry.getKey();
                BotJobAssignment jobAssignment = typeEntry.getValue();

                if (jobAssignment == null) {
                    cacheTypeMap.remove(type);
                    continue;
                }

                cacheTypeMap.put(type, jobAssignment);
            }

            if (cacheTypeMap.isEmpty()) {
                jobAssignmentMap.remove(blockPos);
            }
        }
    }

    public static void clear() {
        networkMap.clear();
        jobAssignmentMap.clear();
    }
}
