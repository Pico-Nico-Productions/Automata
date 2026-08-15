package pro.piconico.automata.bot.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.network.BotNetworkManager.ServerBotNetwork;

public class BotNetworkMap<T extends BotNetwork> extends HashMap<ChunkPos, T> {
    public static final PacketCodec<ByteBuf, BotNetworkMap<?>> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(HashMap::new, ChunkPos.PACKET_CODEC, PacketCodecs.optional(BotNetwork.PACKET_CODEC)), BotNetworkMap::toOptionalMap, BotNetworkMap::new);

    public BotNetworkMap() {
        super();
    }

    public BotNetworkMap(Collection<T> networks) {
        super();

        for (T network : networks) {
            for (ChunkPos chunkPos : network.roboportMap.keySet()) {
                put(chunkPos, network);
            }
        }
    }

    public BotNetworkMap(BotNetworkMap<T> original) {
        super(original);
    }

    private BotNetworkMap(Map<ChunkPos, Optional<T>> optionalNetworkMap) {
        for (Entry<ChunkPos, Optional<T>> networkEntry : optionalNetworkMap.entrySet()) {
            put(networkEntry.getKey(), networkEntry.getValue().orElse(null));
        }
    }

    public static <T extends BotNetwork> BotNetworkMap<T> calculateDelta(BotNetworkMap<T> oldMap, BotNetworkMap<T> newMap) {
        BotNetworkMap<T> deltaMap = new BotNetworkMap<>(newMap);

        for (Entry<ChunkPos, T> networkEntry : oldMap.entrySet()) {
            ChunkPos chunkPos = networkEntry.getKey();

            if (!newMap.containsKey(chunkPos)) {
                deltaMap.put(chunkPos, null);
                continue;
            }

            T oldNetwork = networkEntry.getValue(), newNetwork = newMap.get(chunkPos);

            if (oldNetwork.equals(newNetwork)) {
                deltaMap.remove(chunkPos);
            }
            else if (newNetwork instanceof ServerBotNetwork serverNetwork) {
                if (serverNetwork.isDirty()) {
                    serverNetwork.markNotDirty();
                }
                else {
                    deltaMap.remove(chunkPos);
                }
            }
        }

        return deltaMap;
    }

    private static Map<ChunkPos, Optional<BotNetwork>> toOptionalMap(BotNetworkMap<?> networkMap) {
        Map<ChunkPos, Optional<BotNetwork>> optionalNetworkMap = new HashMap<>();

        for (Entry<ChunkPos, ?> networkEntry : networkMap.entrySet()) {
            optionalNetworkMap.put(networkEntry.getKey(), Optional.ofNullable((BotNetwork)networkEntry.getValue()));
        }

        return optionalNetworkMap;
    }
}
