package pro.piconico.automata.bot.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.network.BotNetworkManager.ServerBotNetwork;

public class BotNetworkUtils {
    public static final PacketCodec<ByteBuf, Map<ChunkPos, Set<BlockPos>>> ROBOPORT_MAP_PACKET_CODEC = PacketCodecs.codec(BlockPos.CODEC.listOf().xmap(BotNetworkUtils::mapRoboports, BotNetworkUtils::flattenRoboports));
    public static final PacketCodec<ByteBuf, Map<ChunkPos, BotNetwork>> NETWORK_MAP_PACKET_CODEC = PacketCodecs.codec(BotNetwork.CODEC.listOf().xmap(BotNetworkUtils::map, BotNetworkUtils::flatten));

    public static List<BlockPos> flattenRoboports(Map<ChunkPos, Set<BlockPos>> roboportMap) {
        return roboportMap.values().stream().flatMap(set -> set.stream()).toList();
    }

    public static Map<ChunkPos, Set<BlockPos>> mapRoboports(Collection<BlockPos> roboports) {
        return roboports.stream().collect(Collectors.groupingBy(roboport -> new ChunkPos(roboport), Collectors.toSet()));
    }

    public static List<BotNetwork> flatten(Map<ChunkPos, BotNetwork> networkMap) {
        return networkMap.values().stream().map(serverNetwork -> (BotNetwork)serverNetwork).distinct().toList();
    }

    public static Map<ChunkPos, BotNetwork> map(Collection<BotNetwork> networks) {
        Map<ChunkPos, BotNetwork> networkMap = new HashMap<>();

        for (BotNetwork botNetwork : networks) {
            for (ChunkPos chunkPos : botNetwork.roboportMap.keySet()) {
                networkMap.put(chunkPos, botNetwork);
            }
        }

        return networkMap;
    }

    public static Map<ChunkPos, BotNetwork> map(Set<ServerBotNetwork> networks) {
        return map(networks.stream().map(net -> (BotNetwork)net).toList());
    }
}
