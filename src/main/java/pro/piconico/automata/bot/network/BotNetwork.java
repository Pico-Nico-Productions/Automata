package pro.piconico.automata.bot.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class BotNetwork {
    public static final PacketCodec<ByteBuf, BotNetwork> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.collection(ArrayList::new, BlockPos.PACKET_CODEC), BotNetwork::flatten, BotNetwork::new);

    protected final Map<ChunkPos, Set<BlockPos>> roboportMap;

    protected BotNetwork(Collection<BlockPos> roboports) {
        roboportMap = map(roboports);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof BotNetwork net)) return false;

        Set<BlockPos> roboports = roboportMap.values().stream().flatMap(s -> s.stream()).collect(Collectors.toSet());
        Set<BlockPos> otherRoboports = net.roboportMap.values().stream().flatMap(s -> s.stream()).collect(Collectors.toSet());
        return roboports.equals(otherRoboports);
    }

    public Set<ChunkPos> getChunks() {
        return Collections.unmodifiableSet(roboportMap.keySet());
    }

    public Stream<BlockPos> getRoboports() {
        return roboportMap.values().stream().flatMap(roboportsInChunk -> roboportsInChunk.stream());
    }

    private List<BlockPos> flatten() {
        return roboportMap.values().stream().flatMap(set -> set.stream()).toList();
    }

    private static Map<ChunkPos, Set<BlockPos>> map(Collection<BlockPos> roboports) {
        return roboports.stream().collect(Collectors.groupingBy(roboport -> new ChunkPos(roboport), Collectors.toSet()));
    }
}
