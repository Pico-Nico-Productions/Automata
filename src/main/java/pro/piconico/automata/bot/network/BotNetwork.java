package pro.piconico.automata.bot.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class BotNetwork {
    public static final PacketCodec<ByteBuf, BotNetwork> PACKET_CODEC = PacketCodec.tuple( //
            PacketCodecs.collection(ArrayList::new, BlockPos.PACKET_CODEC), BotNetwork::getRoboports, //
            PacketCodecs.collection(ArrayList::new, BlockPos.PACKET_CODEC), BotNetwork::getLogisticStorages, //
            Uuids.PACKET_CODEC, net -> net.teamUuid, //
            BotNetwork::new);

    protected final Map<ChunkPos, Set<BlockPos>> roboportMap;
    protected final Map<ChunkPos, Set<BlockPos>> logisticStorageMap;

    public final UUID teamUuid;

    protected BotNetwork(Collection<BlockPos> roboports, Collection<BlockPos> logisticStorages, UUID teamUuid) {
        roboportMap = map(roboports);
        logisticStorageMap = map(logisticStorages);
        this.teamUuid = teamUuid;
    }

    protected BotNetwork(BotNetwork original) {
        roboportMap = original.roboportMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> Set.copyOf(entry.getValue())));
        logisticStorageMap = original.logisticStorageMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> Set.copyOf(entry.getValue())));
        teamUuid = original.teamUuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof BotNetwork network) || !teamUuid.equals(network.teamUuid))
            return false;

        return getRoboports().equals(network.getRoboports()) && getLogisticStorages().equals(network.getRoboports());
    }

    public Set<ChunkPos> getChunks() {
        return Collections.unmodifiableSet(roboportMap.keySet());
    }

    public Stream<BlockPos> streamRoboports() {
        return roboportMap.values().stream().flatMap(roboportsInChunk -> roboportsInChunk.stream());
    }

    public List<BlockPos> getRoboports() {
        return streamRoboports().toList();
    }

    public Stream<BlockPos> streamLogisticStorages() {
        return logisticStorageMap.values().stream().flatMap(logisticStoragesInChunk -> logisticStoragesInChunk.stream());
    }

    public List<BlockPos> getLogisticStorages() {
        return streamLogisticStorages().toList();
    }

    private static Map<ChunkPos, Set<BlockPos>> map(Collection<BlockPos> roboports) {
        return roboports.stream().collect(Collectors.groupingBy(roboport -> new ChunkPos(roboport), Collectors.toSet()));
    }
}
