package pro.piconico.automata.bot.network;

import java.util.Collection;
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
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestStorage.OccupationStatus;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.event.PointOfInterestCallback;
import pro.piconico.automata.registry.AutomataPointOfInterestTypes;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

// TODO: Add unloaded network clearing
public class BotNetworkManager {
    private static Map<ServerWorld, Map<ChunkPos, ServerBotNetwork>> networkMapCache = new HashMap<>();

    public static class ServerBotNetwork extends BotNetwork {
        private final ServerWorld serverWorld;
        private boolean dirty;

        private ServerBotNetwork(Collection<BlockPos> roboports, ServerWorld serverWorld) {
            super(roboports);
            this.serverWorld = serverWorld;
        }

        private ServerBotNetwork(BotNetwork network, ServerWorld serverWorld) {
            super(network.roboportMap);
            this.serverWorld = serverWorld;
        }

        private Set<ServerBotNetwork> getSubnetworks() {
            Set<ServerBotNetwork> subnetworks = new HashSet<>();

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
                subnetworks.add(new ServerBotNetwork(subnetworkRoboports, serverWorld));
            }

            return subnetworks;
        }

        public boolean isDirty() {
            return dirty;
        }

        private void markDirty() {
            dirty = true;
        }

        public void markNotDirty() {
            dirty = false;
        }

        private boolean add(BlockPos roboport) {
            ChunkPos roboportChunk = new ChunkPos(roboport);
            Set<BlockPos> roboportsInChunk = roboportMap.computeIfAbsent(roboportChunk, chunkPos -> new HashSet<>());
            boolean added = roboportsInChunk.add(roboport);

            if (added) {
                markDirty();
            }

            return added;
        }

        private boolean addAll(Iterable<BlockPos> roboports) {
            boolean added = false;

            for (BlockPos roboportBlockPos : roboports) {
                if (add(roboportBlockPos)) {
                    added = true;
                }
            }

            if (added) {
                markDirty();
            }

            return added;
        }

        private record RemovedObjects(Optional<BlockPos> roboport, Optional<ChunkPos> chunk, Set<ServerBotNetwork> subnetworks) {
            public static final RemovedObjects NONE = new RemovedObjects(Optional.empty(), Optional.empty(), Set.of());
        }

        private RemovedObjects remove(BlockPos roboport) {
            ChunkPos roboportChunk = new ChunkPos(roboport);

            if (!roboportMap.containsKey(roboportChunk))
                return RemovedObjects.NONE;

            Set<BlockPos> roboportsInChunk = roboportMap.get(roboportChunk);

            if (!roboportsInChunk.contains(roboport))
                return RemovedObjects.NONE;

            markDirty();

            roboportsInChunk.remove(roboport);
            if (!roboportsInChunk.isEmpty())
                return new RemovedObjects(Optional.of(roboport), Optional.empty(), Set.of());

            roboportMap.remove(roboportChunk);
            Set<ServerBotNetwork> subnetworks = getSubnetworks();

            if (subnetworks.size() <= 1)
                return new RemovedObjects(Optional.of(roboport), Optional.of(roboportChunk), Set.of());

            BotNetwork biggestSubnetwork = subnetworks.stream().max(Comparator.comparingInt(subnetwork -> subnetwork.roboportMap.keySet().size())).get();
            subnetworks.remove(biggestSubnetwork);
            for (BotNetwork subnetwork : subnetworks) {
                subnetwork.roboportMap.keySet().forEach(chunk -> roboportMap.remove(chunk));
            }

            return new RemovedObjects(Optional.of(roboport), Optional.of(roboportChunk), subnetworks);
        }

        private Optional<BotEntity> assignJob(BotJob job) {
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

    private static Optional<ServerBotNetwork> getNetwork(ChunkPos chunkPos, ServerWorld serverWorld) {
        if (!networkMapCache.containsKey(serverWorld))
            return Optional.empty();

        Map<ChunkPos, ServerBotNetwork> worldNetworkMap = networkMapCache.get(serverWorld);

        if (!worldNetworkMap.containsKey(chunkPos))
            return Optional.empty();

        return Optional.of(worldNetworkMap.get(chunkPos));
    }

    private static Set<ServerBotNetwork> getNeighborNetworks(ChunkPos chunkPos, ServerWorld serverWorld) {
        if (!networkMapCache.containsKey(serverWorld))
            return Set.of();

        Set<ServerBotNetwork> networks = new HashSet<>();

        ChunkPos.stream(chunkPos, 1).forEach(chunk -> {
            if (chunk.equals(chunkPos))
                return;

            Optional<ServerBotNetwork> network = getNetwork(chunk, serverWorld);
            if (network.isEmpty())
                return;

            networks.add(network.get());
        });

        return networks;
    }

    private static boolean putNetwork(ServerBotNetwork network, ServerWorld serverWorld) {
        Map<ChunkPos, ServerBotNetwork> worldNetworkMap = networkMapCache.computeIfAbsent(serverWorld, s -> new HashMap<>());
        for (ChunkPos chunkPos : network.roboportMap.keySet()) {
            worldNetworkMap.put(chunkPos, network);
        }

        return true;
    }

    private static Optional<ServerBotNetwork> loadNetwork(ChunkPos chunkPos, ServerWorld serverWorld) {
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

        ServerBotNetwork network = new ServerBotNetwork(roboports, serverWorld);
        putNetwork(network, serverWorld);
        return Optional.of(network);
    }

    private static Optional<ServerBotNetwork> getOrLoadNetwork(ChunkPos chunkPos, ServerWorld serverWorld) {
        Optional<ServerBotNetwork> network = getNetwork(chunkPos, serverWorld);
        if (network.isPresent())
            return network;

        return loadNetwork(chunkPos, serverWorld);
    }

    public static Set<ServerBotNetwork> getOrLoadNetworks(ChunkBounds chunkBounds, ServerWorld serverWorld) {
        Set<ServerBotNetwork> networks = new HashSet<>();

        Set<ChunkPos> networkChunks = new HashSet<>();
        for (ChunkPos chunkPos : chunkBounds.toStream().toList()) {
            if (networkChunks.contains(chunkPos))
                continue;

            Optional<ServerBotNetwork> network = getOrLoadNetwork(chunkPos, serverWorld);
            if (network.isEmpty())
                continue;

            networks.add(network.get());
            networkChunks.addAll(network.get().roboportMap.keySet());
        }

        return networks;
    }

    public static Optional<BotEntity> assignJob(BotJob job, ServerWorld serverWorld) {
        ChunkPos chunkPos = new ChunkPos(job.pos());

        Optional<ServerBotNetwork> network = getOrLoadNetwork(chunkPos, serverWorld);
        if (network.isEmpty())
            return Optional.empty();

        return network.get().assignJob(job);
    }

    public static int clearCache(ServerWorld serverWorld) {
        if (!networkMapCache.containsKey(serverWorld))
            return 0;

        int clearCount = (int)networkMapCache.get(serverWorld).values().stream().distinct().count();

        networkMapCache.remove(serverWorld);

        return clearCount;
    }

    private static void onPointOfInterestAdded(BlockPos pos, RegistryEntry<PointOfInterestType> type, ServerWorld serverWorld) {
        if (type.getKey().get() != AutomataPointOfInterestTypes.ROBOPORT)
            return;

        ChunkPos chunkPos = new ChunkPos(pos);

        Optional<ServerBotNetwork> network = getNetwork(chunkPos, serverWorld);
        if (network.isPresent()) {
            network.get().add(pos);
            NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Add);
            return;
        }

        Set<ServerBotNetwork> neighborNetworks = getNeighborNetworks(chunkPos, serverWorld);

        if (neighborNetworks.isEmpty()) {
            NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Add);
            return;
        }

        ChunkPos.stream(chunkPos, 1).forEach(chunk -> {
            if (chunkPos.equals(chunk) || neighborNetworks.stream().anyMatch(net -> net.roboportMap.containsKey(chunk)))
                return;

            Optional<ServerBotNetwork> net = loadNetwork(chunk, serverWorld);
            if (net.isEmpty())
                return;

            neighborNetworks.add(net.get());
        });

        ServerBotNetwork biggestNeighbor = neighborNetworks.stream().max(Comparator.comparingInt(net -> (int)net.getRoboports().count())).get();
        biggestNeighbor.add(pos);
        neighborNetworks.remove(biggestNeighbor);
        for (ServerBotNetwork neighborNetwork : neighborNetworks) {
            biggestNeighbor.addAll(neighborNetwork.getRoboports().toList());
        }
        putNetwork(biggestNeighbor, serverWorld);
        NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Add);
    }

    private static void onPointOfInterestRemoved(BlockPos pos, RegistryEntry<PointOfInterestType> type, ServerWorld serverWorld) {
        if (type.getKey().get() != AutomataPointOfInterestTypes.ROBOPORT)
            return;

        Optional<ServerBotNetwork> network = getNetwork(new ChunkPos(pos), serverWorld);
        if (network.isEmpty()) {
            NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);
            return;
        }

        ServerBotNetwork.RemovedObjects removedObjects = network.get().remove(pos);
        if (removedObjects == ServerBotNetwork.RemovedObjects.NONE) {
            NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);
            return;
        }

        if (removedObjects.chunk.isPresent()) {
            networkMapCache.get(serverWorld).remove(removedObjects.chunk.get());
        }
        for (ServerBotNetwork subnetwork : removedObjects.subnetworks) {
            putNetwork(subnetwork, serverWorld);
        }

        NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);
    }

    public static void initialize() {
        PointOfInterestCallback.ADDED.register(BotNetworkManager::onPointOfInterestAdded);
        PointOfInterestCallback.REMOVED.register(BotNetworkManager::onPointOfInterestRemoved);
    }
}
