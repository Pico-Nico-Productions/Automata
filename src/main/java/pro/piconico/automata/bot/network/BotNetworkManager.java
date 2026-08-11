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
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestStorage.OccupationStatus;
import pro.piconico.automata.block.RoboportBlock;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataPointOfInterestTypes;

// TODO: Add unloaded network clearing
public class BotNetworkManager {
    private static Map<ServerWorld, Map<ChunkPos, BotNetwork>> networkMap = new HashMap<>();

    private static class BotNetwork {
        public final Map<ChunkPos, Set<BlockPos>> roboportMap = new HashMap<>();
        public final ServerWorld serverWorld;

        public BotNetwork(Set<BlockPos> roboports, ServerWorld serverWorld) {
            if (!addAll(roboports))
                throw new IllegalArgumentException("Can't create an empty network");

            this.serverWorld = serverWorld;
        }

        public Stream<BlockPos> getRoboports() {
            return roboportMap.values().stream().flatMap(roboportsInChunk -> roboportsInChunk.stream());
        }

        public Set<BotNetwork> getSubnetworks() {
            Set<BotNetwork> subnetworks = new HashSet<>();

            Queue<ChunkPos> toSubnet = new LinkedList<>(roboportMap.keySet());
            Set<ChunkPos> subneted = new HashSet<>();
            while (!toSubnet.isEmpty()) {
                ChunkPos subnetingChunk = toSubnet.remove();
                if (subneted.contains(subnetingChunk))
                    continue;

                Set<BlockPos> subnetworkRoboports = new HashSet<>();

                Queue<ChunkPos> toVisit = new LinkedList<>(List.of(subnetingChunk));
                Set<ChunkPos> visited = new HashSet<>(Set.of(subnetingChunk));
                while (!toVisit.isEmpty()) {
                    ChunkPos visitingChunk = toVisit.remove();
                    if (!roboportMap.keySet().contains(visitingChunk))
                        continue;

                    subnetworkRoboports.addAll(roboportMap.get(visitingChunk));
                    ChunkPos.stream(visitingChunk, 1).forEach(pos -> {
                        if (visited.add(pos))
                            toVisit.add(pos);
                    });
                }

                subneted.addAll(subnetworkRoboports.stream().map(roboportPos -> new ChunkPos(roboportPos)).toList());
                subnetworks.add(new BotNetwork(subnetworkRoboports, serverWorld));
            }

            return subnetworks;
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

        public record RemovedObjects(Optional<BlockPos> roboport, Optional<ChunkPos> chunk, Set<BotNetwork> subnetworks) {
            public static final RemovedObjects NONE = new RemovedObjects(Optional.empty(), Optional.empty(), Set.of());
        }

        public RemovedObjects remove(BlockPos roboport) {
            ChunkPos roboportChunk = new ChunkPos(roboport);

            if (!roboportMap.containsKey(roboportChunk))
                return RemovedObjects.NONE;

            Set<BlockPos> roboportsInChunk = roboportMap.get(roboportChunk);

            if (!roboportsInChunk.contains(roboport))
                return RemovedObjects.NONE;

            roboportsInChunk.remove(roboport);
            if (!roboportsInChunk.isEmpty())
                return new RemovedObjects(Optional.of(roboport), Optional.empty(), Set.of());

            roboportMap.remove(roboportChunk);
            Set<BotNetwork> subnetworks = getSubnetworks();

            if (subnetworks.size() <= 1)
                return new RemovedObjects(Optional.of(roboport), Optional.of(roboportChunk), Set.of());

            BotNetwork biggestSubnetwork = subnetworks.stream().max(Comparator.comparingInt(subnetwork -> subnetwork.roboportMap.keySet().size())).get();
            subnetworks.remove(biggestSubnetwork);
            for (BotNetwork subnetwork : subnetworks) {
                subnetwork.roboportMap.keySet().forEach(chunk -> roboportMap.remove(chunk));
            }

            return new RemovedObjects(Optional.of(roboport), Optional.of(roboportChunk), subnetworks);
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

    public enum Mutation {
        Add, Remove, Modify
    }

    @FunctionalInterface
    public interface Mutate {
        void onMutate(ServerWorld serverWorld, Mutation mutation);
    }

    public static final Event<Mutate> NETWORKS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (serverWorld, mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(serverWorld, mutation);
        }
    });

    private static Optional<BotNetwork> getNetworkAt(ChunkPos chunkPos, ServerWorld serverWorld) {
        if (!networkMap.containsKey(serverWorld))
            return Optional.empty();

        Map<ChunkPos, BotNetwork> worldNetworkMap = networkMap.get(serverWorld);

        if (!worldNetworkMap.containsKey(chunkPos))
            return Optional.empty();

        return Optional.of(worldNetworkMap.get(chunkPos));
    }

    private static Set<BotNetwork> getNeighborNetworks(ChunkPos chunkPos, ServerWorld serverWorld) {
        if (!networkMap.containsKey(serverWorld))
            return Set.of();

        Set<BotNetwork> networks = new HashSet<>();

        ChunkPos.stream(chunkPos, 1).forEach(chunk -> {
            if (chunk.equals(chunkPos))
                return;

            Optional<BotNetwork> network = getNetworkAt(chunk, serverWorld);
            if (network.isEmpty())
                return;

            networks.add(network.get());
        });

        return networks;
    }

    private static boolean putNetwork(BotNetwork network, ServerWorld serverWorld) {
        Map<ChunkPos, BotNetwork> worldNetworkMap = networkMap.computeIfAbsent(serverWorld, s -> new HashMap<>());
        for (ChunkPos chunkPos : network.roboportMap.keySet()) {
            worldNetworkMap.put(chunkPos, network);
        }

        return true;
    }

    private static Optional<BotNetwork> loadNetwork(ChunkPos chunkPos, ServerWorld serverWorld) {
        Set<BlockPos> roboports = new HashSet<>();

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

            roboports.addAll(roboportsInChunk);
            ChunkPos.stream(visitingChunk, 1).forEach(pos -> {
                if (visited.add(pos))
                    toVisit.add(pos);
            });
        }

        if (roboports.isEmpty())
            return Optional.empty();

        BotNetwork newNetwork = new BotNetwork(roboports, serverWorld);
        putNetwork(newNetwork, serverWorld);
        return Optional.of(newNetwork);
    }

    public static Optional<BotEntity> assignJob(BotJob job, ServerWorld serverWorld) {
        ChunkPos chunkPos = new ChunkPos(job.pos());

        Optional<BotNetwork> network = getNetworkAt(chunkPos, serverWorld);
        if (network.isPresent())
            return network.get().assignJob(job);

        Optional<BotNetwork> newNetwork = loadNetwork(chunkPos, serverWorld);
        if (newNetwork.isEmpty())
            return Optional.empty();

        return newNetwork.get().assignJob(job);
    }

    public static int clearCache(ServerWorld serverWorld) {
        if (!networkMap.containsKey(serverWorld))
            return 0;

        int clearCount = (int)networkMap.get(serverWorld).values().stream().distinct().count();

        networkMap.remove(serverWorld);

        return clearCount;
    }

    private static void onRoboportPlaced(BlockPos roboportPos, ServerWorld serverWorld) {
        ChunkPos chunkPos = new ChunkPos(roboportPos);

        Optional<BotNetwork> network = getNetworkAt(chunkPos, serverWorld);
        if (network.isPresent()) {
            network.get().add(roboportPos);
            NETWORKS_MUTATED.invoker().onMutate(network.get().serverWorld, Mutation.Add);
            return;
        }

        Set<BotNetwork> neighborNetworks = getNeighborNetworks(chunkPos, serverWorld);

        if (neighborNetworks.isEmpty())
            return;

        ChunkPos.stream(chunkPos, 1).forEach(chunk -> {
            if (chunkPos.equals(chunk) || neighborNetworks.stream().anyMatch(net -> net.roboportMap.containsKey(chunk)))
                return;

            Optional<BotNetwork> net = loadNetwork(chunk, serverWorld);
            if (net.isEmpty())
                return;

            neighborNetworks.add(net.get());
        });

        BotNetwork biggestNeighbor = neighborNetworks.stream().max(Comparator.comparingInt(net -> (int)net.getRoboports().count())).get();
        biggestNeighbor.add(roboportPos);
        neighborNetworks.remove(biggestNeighbor);
        for (BotNetwork neighborNetwork : neighborNetworks) {
            biggestNeighbor.addAll(neighborNetwork.getRoboports().toList());
        }
        putNetwork(biggestNeighbor, serverWorld);
        NETWORKS_MUTATED.invoker().onMutate(biggestNeighbor.serverWorld, Mutation.Add);
    }

    private static void onRoboportRemoved(BlockPos roboportPos, ServerWorld serverWorld) {
        Optional<BotNetwork> network = getNetworkAt(new ChunkPos(roboportPos), serverWorld);
        if (network.isEmpty())
            return;

        BotNetwork.RemovedObjects removedObjects = network.get().remove(roboportPos);
        if (removedObjects == BotNetwork.RemovedObjects.NONE)
            return;

        if (removedObjects.chunk.isPresent()) {
            networkMap.get(serverWorld).remove(removedObjects.chunk.get());
        }
        for (BotNetwork subnetwork : removedObjects.subnetworks) {
            putNetwork(subnetwork, serverWorld);
        }

        NETWORKS_MUTATED.invoker().onMutate(network.get().serverWorld, Mutation.Remove);
    }

    public static void initialize() {
        RoboportBlock.PLACED.register(BotNetworkManager::onRoboportPlaced);
        RoboportBlock.REMOVED.register(BotNetworkManager::onRoboportRemoved);
    }
}
