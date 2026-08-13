package pro.piconico.automata.bot.network;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class BotNetwork {
    public static final Codec<BotNetwork> CODEC = RecordCodecBuilder.create(instance -> instance.group(BlockPos.CODEC.listOf().fieldOf("roboports").forGetter(net -> BotNetworkUtils.flattenRoboports(net.roboportMap))).apply(instance, BotNetwork::new));

    protected final Map<ChunkPos, Set<BlockPos>> roboportMap;

    protected BotNetwork(Collection<BlockPos> roboports) {
        roboportMap = BotNetworkUtils.mapRoboports(roboports);
    }

    protected BotNetwork(Map<ChunkPos, Set<BlockPos>> roboportMap) {
        this.roboportMap = roboportMap;
    }

    public Set<ChunkPos> getChunks() {
        return Collections.unmodifiableSet(roboportMap.keySet());
    }

    public Stream<BlockPos> getRoboports() {
        return roboportMap.values().stream().flatMap(roboportsInChunk -> roboportsInChunk.stream());
    }
}
