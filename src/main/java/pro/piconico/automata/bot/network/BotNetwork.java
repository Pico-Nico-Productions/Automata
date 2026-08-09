package pro.piconico.automata.bot.network;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestStorage.OccupationStatus;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataPointOfInterestTypes;

public class BotNetwork {
    private final Map<ChunkPos, Set<BlockPos>> roboportMap = new HashMap<>();
    private final ServerWorld serverWorld;

    public BotNetwork(ChunkPos chunkPos, ServerWorld serverWorld) {
        Queue<ChunkPos> toVisit = new LinkedList<>(List.of(chunkPos));
        Set<ChunkPos> visited = new HashSet<>(Set.of(chunkPos));
        PointOfInterestStorage pointOfInterestStorage = serverWorld.getPointOfInterestStorage();
        while (!toVisit.isEmpty()) {
            ChunkPos visitingChunk = toVisit.remove();
            List<BlockPos> roboportsInChunk = pointOfInterestStorage
                    .getInChunk(entry -> entry.matchesKey(AutomataPointOfInterestTypes.ROBOPORT), visitingChunk, OccupationStatus.ANY)
                    .map(PointOfInterest::getPos).toList();
            if (roboportsInChunk.isEmpty())
                continue;

            addAll(roboportsInChunk);
            ChunkPos.stream(visitingChunk, 1).forEach(pos -> {
                if (visited.add(pos))
                    toVisit.add(pos);
            });
        }

        this.serverWorld = serverWorld;
    }

    public boolean isEmpty() {
        return roboportMap.isEmpty();
    }

    public Set<ChunkPos> getChunks() {
        return roboportMap.keySet();
    }

    public boolean add(BlockPos roboport) {
        ChunkPos roboportChunk = new ChunkPos(roboport);
        Set<BlockPos> roboportsInChunk = roboportMap.computeIfAbsent(roboportChunk, chunkPos -> new HashSet<>());
        return roboportsInChunk.add(roboport);
    }

    public boolean addAll(Iterable<BlockPos> roboports) {
        boolean added = false;
        for (BlockPos roboportBlockPos : roboports) {
            if (add(roboportBlockPos))
                added = true;
        }

        return added;
    }

    public Optional<BotEntity> assignJob(BotJob job) {
        Stream<BlockPos> capableRoboports = roboportMap.values().stream().flatMap(roboportsInChunk -> roboportsInChunk.stream())
                .filter(roboportPos -> serverWorld.getBlockEntity(roboportPos) instanceof RoboportBlockEntity roboport && roboport.canAssignJob(job));
        Optional<BlockPos> closestCapableRoboport = capableRoboports.min(Comparator.comparingInt(pos -> pos.getChebyshevDistance(job.pos())));

        if (closestCapableRoboport.isEmpty())
            return Optional.empty();

        return ((RoboportBlockEntity)serverWorld.getBlockEntity(closestCapableRoboport.get())).assignJob(job);
    }
}
