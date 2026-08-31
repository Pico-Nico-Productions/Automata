package pro.piconico.automata.bot.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
            PacketCodecs.collection(ArrayList::new, BlockPos.PACKET_CODEC), BotNetwork::flatten, //
            Uuids.PACKET_CODEC, net -> net.teamUuid, //
            BotNetwork::new);

    protected final Map<ChunkPos, Set<BlockPos>> roboportMap;

    public final UUID teamUuid;

    protected BotNetwork(Collection<BlockPos> roboports, UUID teamUuid) {
        roboportMap = map(roboports);
        this.teamUuid = teamUuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof BotNetwork net) || !teamUuid.equals(net.teamUuid))
            return false;

        Set<BlockPos> roboports = getRoboports().collect(Collectors.toSet());
        Set<BlockPos> otherRoboports = net.getRoboports().collect(Collectors.toSet());
        return roboports.equals(otherRoboports);
    }

    public Set<ChunkPos> getChunks() {
        return Collections.unmodifiableSet(roboportMap.keySet());
    }

    public Stream<BlockPos> getRoboports() {
        return roboportMap.values().stream().flatMap(roboportsInChunk -> roboportsInChunk.stream());
    }

    private List<BlockPos> flatten() {
        return getRoboports().toList();
    }

    private static Map<ChunkPos, Set<BlockPos>> map(Collection<BlockPos> roboports) {
        return roboports.stream().collect(Collectors.groupingBy(roboport -> new ChunkPos(roboport), Collectors.toSet()));
    }
}
