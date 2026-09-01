package pro.piconico.automata.client.network;

import java.util.Map;
import java.util.function.BiConsumer;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobAssignmentMap;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.network.BotNetwork;
import pro.piconico.automata.bot.network.BotNetworkMap;
import pro.piconico.automata.util.MapUtils;

public class BotCache {
    public static final BotNetworkMap<BotNetwork> networkMap = new BotNetworkMap<>();
    public static final BotJobAssignmentMap jobAssignmentMap = new BotJobAssignmentMap();

    @SuppressWarnings("unchecked")
    public static void applyDelta(BotNetworkMap<?> deltaMap) {
        MapUtils.applyMapDelta(networkMap, (BotNetworkMap<BotNetwork>)deltaMap, null);
    }

    public static void applyDelta(BotJobAssignmentMap deltaMap) {
        BiConsumer<Map<BotJobType<?>, BotJobAssignment>, Map<BotJobType<?>, BotJobAssignment>> applyTypeLayer = (targetTypeMap, deltaTypeMap) -> MapUtils
                .applyMapDelta(targetTypeMap, deltaTypeMap, null);

        MapUtils.applyMapDelta(jobAssignmentMap, deltaMap, applyTypeLayer);
    }

    public static void clear() {
        networkMap.clear();
        jobAssignmentMap.clear();
    }
}
