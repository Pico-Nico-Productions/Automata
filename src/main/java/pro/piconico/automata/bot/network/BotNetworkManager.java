package pro.piconico.automata.bot.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

// TODO: Add unloaded network clearing
public class BotNetworkManager {
    private static Map<ChunkPos, BotNetwork> networkMap = new HashMap<>();

    public static Optional<BotNetwork> getNetworkAt(ChunkPos chunkPos, ServerWorld serverWorld) {
        if (networkMap.containsKey(chunkPos))
            return Optional.of(networkMap.get(chunkPos));

        BotNetwork botNetwork = new BotNetwork(chunkPos, serverWorld);

        if (botNetwork.isEmpty())
            return Optional.empty();

        for (ChunkPos visitingChunk : botNetwork.getChunks()) {
            networkMap.put(visitingChunk, botNetwork);
        }

        return Optional.of(botNetwork);
    }

    public static Optional<BotNetwork> getNetworkAt(BlockPos blockPos, ServerWorld serverWorld) {
        return getNetworkAt(new ChunkPos(blockPos), serverWorld);
    }

    public static void initialize() {
        // TODO: Add roboport event listeners that update network map
    }
}
