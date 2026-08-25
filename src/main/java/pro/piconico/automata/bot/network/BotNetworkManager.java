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
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
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

public class BotNetworkManager {
    private static final Map<ServerWorld, BotNetworkMap<ServerBotNetwork>> NETWORK_MAP_CACHE = new HashMap<>();

    public enum Mutation {
        Add, Remove
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

    public static class ServerBotNetwork extends BotNetwork {
        private final ServerWorld serverWorld;
        private boolean dirty;

        private ServerBotNetwork(Collection<BlockPos> roboports, ServerWorld serverWorld) {
            super(roboports);
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

                ServerBotNetwork serverNetwork = new ServerBotNetwork(subnetworkRoboports, serverWorld);
                if (equals(serverNetwork))
                    return Set.of(this);

                subnetworks.add(serverNetwork);
                subneted.addAll(subnetworkRoboports.stream().map(roboportPos -> new ChunkPos(roboportPos)).toList());
            }

            return subnetworks;
        }

        public boolean isDirty() {
            return dirty;
        }

        private void markDirty() {
            dirty = true;
        }

        private void markNotDirty() {
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

    //#region Network Fetching
    public static Optional<ServerBotNetwork> getNetwork(ChunkPos chunkPos, ServerWorld serverWorld) {
        if (!NETWORK_MAP_CACHE.containsKey(serverWorld))
            return Optional.empty();

        BotNetworkMap<ServerBotNetwork> worldNetworkMap = NETWORK_MAP_CACHE.get(serverWorld);

        if (!worldNetworkMap.containsKey(chunkPos))
            return Optional.empty();

        return Optional.of(worldNetworkMap.get(chunkPos));
    }

    private static boolean putNetwork(ServerBotNetwork serverNetwork) {
        BotNetworkMap<ServerBotNetwork> worldNetworkMap = NETWORK_MAP_CACHE.computeIfAbsent(serverNetwork.serverWorld, s -> new BotNetworkMap<>());
        for (ChunkPos chunkPos : serverNetwork.roboportMap.keySet()) {
            worldNetworkMap.put(chunkPos, serverNetwork);
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

        ServerBotNetwork serverNetwork = new ServerBotNetwork(roboports, serverWorld);
        putNetwork(serverNetwork);
        return Optional.of(serverNetwork);
    }

    private static Optional<ServerBotNetwork> getOrLoadNetwork(ChunkPos chunkPos, ServerWorld serverWorld) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, serverWorld);
        if (serverNetwork.isPresent())
            return serverNetwork;

        return loadNetwork(chunkPos, serverWorld);
    }

    private static Set<ServerBotNetwork> fetchNetworks(Iterable<ChunkPos> chunks, BiFunction<ChunkPos, ServerWorld, Optional<ServerBotNetwork>> fetchNetwork,
            ServerWorld serverWorld) {
        if (!NETWORK_MAP_CACHE.containsKey(serverWorld))
            return Set.of();

        Set<ServerBotNetwork> networks = new HashSet<>();

        Set<ChunkPos> networkChunks = new HashSet<>();
        for (ChunkPos chunkPos : chunks) {
            if (networkChunks.contains(chunkPos))
                continue;

            Optional<ServerBotNetwork> serverNetwork = fetchNetwork.apply(chunkPos, serverWorld);
            if (serverNetwork.isEmpty())
                continue;

            networks.add(serverNetwork.get());
            networkChunks.addAll(serverNetwork.get().roboportMap.keySet());
        }

        return networks;
    }

    public static Set<ServerBotNetwork> getNetworks(ChunkBounds chunkBounds, ServerWorld serverWorld) {
        return fetchNetworks(chunkBounds.toStream().toList(), BotNetworkManager::getNetwork, serverWorld);
    }
    //#endregion

    //#region Operations
    public static void markAllNotDirty(ServerWorld serverWorld) {
        if (!NETWORK_MAP_CACHE.containsKey(serverWorld))
            return;

        for (ServerBotNetwork serverNetwork : NETWORK_MAP_CACHE.get(serverWorld).values()) {
            serverNetwork.markNotDirty();
        }
    }

    public static Optional<BotEntity> assignJob(BotJob job, ServerWorld serverWorld) {
        ChunkPos chunkPos = new ChunkPos(job.pos());

        Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, serverWorld);
        if (serverNetwork.isEmpty())
            return Optional.empty();

        return serverNetwork.get().assignJob(job);
    }
    //#endregion

    //#region Mutations
    private static void onChunkLoaded(ServerWorld serverWorld, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();

        Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, serverWorld);
        if (serverNetwork.isPresent())
            return;

        serverNetwork = loadNetwork(chunkPos, serverWorld);
        if (serverNetwork.isEmpty())
            return;

        NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Add);
    }

    private static void onChunkUnloaded(ServerWorld serverWorld, WorldChunk chunk) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(chunk.getPos(), serverWorld);
        if (serverNetwork.isEmpty())
            return;

        Set<ChunkPos> chunks = serverNetwork.get().getChunks();

        if (chunks.stream().anyMatch(chunkPos -> serverWorld.isChunkLoaded(chunkPos.x, chunkPos.z)))
            return;

        BotNetworkMap<ServerBotNetwork> worldNetworkMap = NETWORK_MAP_CACHE.get(serverWorld);
        for (ChunkPos chunkPos : chunks) {
            worldNetworkMap.remove(chunkPos);
        }
        if (worldNetworkMap.isEmpty()) {
            NETWORK_MAP_CACHE.remove(serverWorld);
        }

        NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);
    }

    private static void onPointOfInterestAdded(BlockPos pos, RegistryEntry<PointOfInterestType> type, ServerWorld serverWorld) {
        if (type.getKey().get() != AutomataPointOfInterestTypes.ROBOPORT)
            return;

        ChunkPos chunkPos = new ChunkPos(pos);

        Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, serverWorld);
        if (serverNetwork.isPresent()) {
            serverNetwork.get().add(pos);
            NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Add);
            return;
        }

        List<ChunkPos> neighborChunks = ChunkPos.stream(chunkPos, 1).filter(chunk -> !chunk.equals(chunkPos)).toList();
        Set<ServerBotNetwork> neighborNetworks = fetchNetworks(neighborChunks, BotNetworkManager::getNetwork, serverWorld);

        if (neighborNetworks.isEmpty() && !serverWorld.isChunkLoaded(chunkPos.x, chunkPos.z))
            return;

        neighborNetworks.addAll(fetchNetworks(neighborChunks, BotNetworkManager::getOrLoadNetwork, serverWorld));

        if (neighborNetworks.isEmpty()) {
            putNetwork(new ServerBotNetwork(List.of(pos), serverWorld));
            NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Add);
            return;
        }

        ServerBotNetwork biggestNeighbor = neighborNetworks.stream().max(Comparator.comparingInt(net -> (int)net.getRoboports().count())).get();
        biggestNeighbor.add(pos);
        neighborNetworks.remove(biggestNeighbor);
        for (ServerBotNetwork neighborNetwork : neighborNetworks) {
            biggestNeighbor.addAll(neighborNetwork.getRoboports().toList());
        }
        putNetwork(biggestNeighbor);
        NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Add);
    }

    private static void onPointOfInterestRemoved(BlockPos pos, RegistryEntry<PointOfInterestType> type, ServerWorld serverWorld) {
        if (type.getKey().get() != AutomataPointOfInterestTypes.ROBOPORT)
            return;

        Optional<ServerBotNetwork> serverNetwork = getNetwork(new ChunkPos(pos), serverWorld);

        if (serverNetwork.isEmpty())
            return;

        ServerBotNetwork.RemovedObjects removedObjects = serverNetwork.get().remove(pos);

        if (removedObjects == ServerBotNetwork.RemovedObjects.NONE)
            return;

        BotNetworkMap<ServerBotNetwork> worldNetworkMap = NETWORK_MAP_CACHE.get(serverWorld);
        if (removedObjects.chunk.isPresent()) {
            worldNetworkMap.remove(removedObjects.chunk.get());
        }
        for (ServerBotNetwork subnetwork : removedObjects.subnetworks) {
            putNetwork(subnetwork);
        }
        if (worldNetworkMap.isEmpty()) {
            NETWORK_MAP_CACHE.remove(serverWorld);
        }

        NETWORKS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);
    }
    //#region Network Updating

    public static void initialize() {
        ServerChunkEvents.CHUNK_LOAD.register(BotNetworkManager::onChunkLoaded);
        ServerChunkEvents.CHUNK_UNLOAD.register(BotNetworkManager::onChunkUnloaded);
        PointOfInterestCallback.ADDED.register(BotNetworkManager::onPointOfInterestAdded);
        PointOfInterestCallback.REMOVED.register(BotNetworkManager::onPointOfInterestRemoved);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            NETWORK_MAP_CACHE.clear();
        });
    }
}
