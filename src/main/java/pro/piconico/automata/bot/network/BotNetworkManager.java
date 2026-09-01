package pro.piconico.automata.bot.network;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.TriFunction;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.poi.PointOfInterestType;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.event.PointOfInterestCallback;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataPointOfInterestTypes;
import pro.piconico.automata.util.MapUtils;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;
import pro.piconico.automata.world.BotTeamPersistentState;

public class BotNetworkManager {
    private static final Map<ServerWorld, Map<UUID, BotNetworkMap<ServerBotNetwork>>> NETWORK_MAP_CACHE = new HashMap<>();
    private static final Map<ServerWorld, Map<UUID, NetworkLoader>> NETWORK_LOADER_CACHE = new HashMap<>();

    public enum Mutation {
        ADD, REMOVE
    }

    @FunctionalInterface
    public interface Mutate {
        void onMutate(UUID teamUuid, ServerWorld serverWorld, Mutation mutation);
    }

    public static final Event<Mutate> NETWORKS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (teamUuid, serverWorld, mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(teamUuid, serverWorld, mutation);
        }
    });

    public static class ServerBotNetwork extends BotNetwork {
        private final ServerWorld serverWorld;
        private boolean dirty;

        private ServerBotNetwork(Collection<BlockPos> roboports, UUID teamUuid, ServerWorld serverWorld) {
            super(roboports, teamUuid);
            this.serverWorld = serverWorld;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;

            if (!(obj instanceof ServerBotNetwork serverNet) || !serverWorld.equals(serverNet.serverWorld))
                return false;

            return super.equals(obj);
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

                ServerBotNetwork serverNetwork = new ServerBotNetwork(subnetworkRoboports, teamUuid, serverWorld);
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

    private static class NetworkLoader {
        private final Set<ChunkPos> requested = new HashSet<>();
        private final Set<ChunkPos> loaded = new HashSet<>();
        private final Set<BlockPos> roboports = new HashSet<>();

        public final ServerWorld serverWorld;
        public final UUID teamUuid;

        public NetworkLoader(ServerWorld serverWorld, UUID teamUuid, WorldChunk chunk) {
            this.serverWorld = serverWorld;
            this.teamUuid = teamUuid;
            load(chunk);
        }

        public boolean isComplete() {
            return requested.size() == loaded.size();
        }

        public Set<BlockPos> getRoboports() {
            return Collections.unmodifiableSet(roboports);
        }

        public void load(WorldChunk chunk) {
            ChunkPos chunkPos = chunk.getPos();
            boolean added = false;

            requested.add(chunkPos);
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (!(blockEntity instanceof RoboportBlockEntity roboport) || roboport.getTeam().filter(teamUuid::equals).isEmpty())
                    continue;

                added |= roboports.add(roboport.getPos());
            }
            loaded.add(chunkPos);

            if (!added)
                return;

            for (ChunkPos pos : ChunkPos.stream(chunkPos, 1).collect(Collectors.toSet())) {
                if (!requested.add(pos))
                    continue;

                serverWorld.getChunkManager().addTicket(ChunkTicketType.PLAYER_SPAWN, pos, 0);
            }
        }

        public void process() {
            Set<ChunkPos> toLoad = new HashSet<>(requested);
            toLoad.removeAll(loaded);

            for (ChunkPos chunkPos : toLoad) {
                WorldChunk worldChunk = serverWorld.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z);

                if (worldChunk == null)
                    continue;

                load(worldChunk);
            }
        }
    }

    //#region Network Fetching
    public static Optional<ServerBotNetwork> getNetwork(ChunkPos chunkPos, UUID teamUuid, ServerWorld serverWorld) {
        if (!NETWORK_MAP_CACHE.containsKey(serverWorld))
            return Optional.empty();

        Map<UUID, BotNetworkMap<ServerBotNetwork>> teamMap = NETWORK_MAP_CACHE.get(serverWorld);

        if (!teamMap.containsKey(teamUuid))
            return Optional.empty();

        BotNetworkMap<ServerBotNetwork> networkMap = teamMap.get(teamUuid);

        if (!networkMap.containsKey(chunkPos))
            return Optional.empty();

        return Optional.of(networkMap.get(chunkPos));
    }

    private static Set<ServerBotNetwork> fetchNetworks(Iterable<ChunkPos> chunks,
            TriFunction<ChunkPos, UUID, ServerWorld, Optional<ServerBotNetwork>> fetchNetwork, UUID teamUuid, ServerWorld serverWorld) {
        Set<ServerBotNetwork> networks = new HashSet<>();

        Set<ChunkPos> networkChunks = new HashSet<>();
        for (ChunkPos chunkPos : chunks) {
            if (networkChunks.contains(chunkPos))
                continue;

            Optional<ServerBotNetwork> serverNetwork = fetchNetwork.apply(chunkPos, teamUuid, serverWorld);
            if (serverNetwork.isEmpty())
                continue;

            networks.add(serverNetwork.get());
            networkChunks.addAll(serverNetwork.get().roboportMap.keySet());
        }

        return networks;
    }

    public static Set<ServerBotNetwork> getNetworks(ChunkBounds chunkBounds, UUID teamUuid, ServerWorld serverWorld) {
        return fetchNetworks(chunkBounds.toStream().toList(), BotNetworkManager::getNetwork, teamUuid, serverWorld);
    }
    //#endregion

    //#region Mutation Operations
    private static void putNetwork(ServerBotNetwork serverNetwork) {
        Map<UUID, BotNetworkMap<ServerBotNetwork>> teamMap = NETWORK_MAP_CACHE.computeIfAbsent(serverNetwork.serverWorld, serverWorld -> new HashMap<>());
        BotNetworkMap<ServerBotNetwork> networkMap = teamMap.computeIfAbsent(serverNetwork.teamUuid, uuid -> new BotNetworkMap<>());
        for (ChunkPos chunkPos : serverNetwork.roboportMap.keySet()) {
            networkMap.put(chunkPos, serverNetwork);
            serverNetwork.serverWorld.getChunkManager().addTicket(ChunkTicketType.PLAYER_LOADING, chunkPos, 0);
        }
    }

    private static void loadNetwork(ServerWorld serverWorld, UUID teamUuid, WorldChunk chunk) {
        Optional<NetworkLoader> networkLoader = MapUtils.getNested(NETWORK_LOADER_CACHE, serverWorld, teamUuid);
        if (networkLoader.isPresent()) {
            networkLoader.get().load(chunk);
            return;
        }

        NetworkLoader newNetworkLoader = new NetworkLoader(serverWorld, teamUuid, chunk);
        if (newNetworkLoader.isComplete())
            return;

        Map<UUID, NetworkLoader> teamMap = NETWORK_LOADER_CACHE.computeIfAbsent(serverWorld, s -> new HashMap<>());
        teamMap.put(teamUuid, newNetworkLoader);
    }

    private static void cacheLoadedNetwork(NetworkLoader networkLoader) {
        Set<ServerBotNetwork> serverNetworks = new ServerBotNetwork(networkLoader.getRoboports(), networkLoader.teamUuid, networkLoader.serverWorld)
                .getSubnetworks();
        for (ServerBotNetwork serverNetwork : serverNetworks) {
            putNetwork(serverNetwork);
        }
        MapUtils.<NetworkLoader>removeNested(NETWORK_LOADER_CACHE, networkLoader.serverWorld, networkLoader.teamUuid);

        NETWORKS_MUTATED.invoker().onMutate(networkLoader.teamUuid, networkLoader.serverWorld, Mutation.ADD);
    }

    private static void unloadNetwork(ServerBotNetwork serverNetwork) {
        Map<UUID, BotNetworkMap<ServerBotNetwork>> teamMap = NETWORK_MAP_CACHE.get(serverNetwork.serverWorld);
        BotNetworkMap<ServerBotNetwork> networkMap = teamMap.get(serverNetwork.teamUuid);
        for (ChunkPos chunkPos : serverNetwork.getChunks()) {
            networkMap.remove(chunkPos);
            serverNetwork.serverWorld.setChunkForced(chunkPos.x, chunkPos.z, false);
        }
        if (networkMap.isEmpty()) {
            teamMap.remove(serverNetwork.teamUuid);
            if (teamMap.isEmpty()) {
                NETWORK_MAP_CACHE.remove(serverNetwork.serverWorld);
            }
        }

        NETWORKS_MUTATED.invoker().onMutate(serverNetwork.teamUuid, serverNetwork.serverWorld, Mutation.REMOVE);
    }

    private static void addRoboport(BlockPos pos, UUID teamUuid, ServerWorld serverWorld) {
        ChunkPos chunkPos = new ChunkPos(pos);

        Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, teamUuid, serverWorld);
        if (serverNetwork.isPresent()) {
            serverNetwork.get().add(pos);
            NETWORKS_MUTATED.invoker().onMutate(teamUuid, serverWorld, Mutation.ADD);
            return;
        }

        WorldChunk worldChunk = serverWorld.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z);

        if (worldChunk == null)
            return;

        loadNetwork(serverWorld, teamUuid, worldChunk);
    }

    private static void removeRoboport(BlockPos pos, UUID teamUuid, ServerWorld serverWorld) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(new ChunkPos(pos), teamUuid, serverWorld);

        if (serverNetwork.isEmpty())
            return;

        ServerBotNetwork.RemovedObjects removedObjects = serverNetwork.get().remove(pos);

        if (removedObjects == ServerBotNetwork.RemovedObjects.NONE)
            return;

        if (removedObjects.chunk.isPresent()) {
            MapUtils.<ServerBotNetwork>removeNested(NETWORK_MAP_CACHE, serverWorld, teamUuid, removedObjects.chunk.get());
        }
        for (ServerBotNetwork subnetwork : removedObjects.subnetworks) {
            putNetwork(subnetwork);
        }

        NETWORKS_MUTATED.invoker().onMutate(teamUuid, serverWorld, Mutation.REMOVE);
    }

    public static void markAllNotDirty(ServerWorld serverWorld) {
        if (!NETWORK_MAP_CACHE.containsKey(serverWorld))
            return;

        for (BotNetworkMap<ServerBotNetwork> networkMap : NETWORK_MAP_CACHE.get(serverWorld).values()) {
            for (ServerBotNetwork serverNetwork : networkMap.values()) {
                serverNetwork.markNotDirty();
            }
        }
    }

    public static Optional<BotEntity> assignJob(BotJob job, UUID teamUuid, ServerWorld serverWorld) {
        ChunkPos chunkPos = new ChunkPos(job.pos());

        Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, teamUuid, serverWorld);
        if (serverNetwork.isEmpty())
            return Optional.empty();

        return serverNetwork.get().assignJob(job);
    }
    //#endregion

    //#region Event Listeners
    private static void onChunkLoaded(ServerWorld serverWorld, WorldChunk chunk) {
        for (UUID teamUuid : BotTeamPersistentState.getTeamMap().keySet()) {
            loadNetwork(serverWorld, teamUuid, chunk);
        }
    }

    private static void onChunkUnloaded(ServerWorld serverWorld, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        for (UUID teamUuid : BotTeamPersistentState.getTeamMap().keySet()) {
            Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, teamUuid, serverWorld);

            if (serverNetwork.isEmpty())
                continue;

            serverWorld.setChunkForced(chunkPos.x, chunkPos.z, true);
            if (serverNetwork.get().getChunks().stream()
                    .anyMatch(pos -> serverWorld.isChunkLoaded(pos.x, pos.z) && !serverWorld.getForcedChunks().contains(pos.toLong()))) {
                continue;
            }

            unloadNetwork(serverNetwork.get());
        }
    }

    private static void onEndWorldTick(ServerWorld serverWorld) {
        if (NETWORK_LOADER_CACHE.isEmpty())
            return;

        for (UUID teamUuid : BotTeamPersistentState.getTeamMap().keySet()) {
            Optional<NetworkLoader> networkLoader = MapUtils.getNested(NETWORK_LOADER_CACHE, serverWorld, teamUuid);
            if (networkLoader.isEmpty())
                continue;

            networkLoader.get().process();

            if (!networkLoader.get().isComplete())
                continue;

            cacheLoadedNetwork(networkLoader.get());
        }
    }

    private static void onPointOfInterestAdded(BlockPos pos, RegistryEntry<PointOfInterestType> type, ServerWorld serverWorld) {
        if (type.getKey().get() != AutomataPointOfInterestTypes.ROBOPORT)
            return;

        Optional<RoboportBlockEntity> roboport = serverWorld.getBlockEntity(pos, AutomataEntities.ROBOPORT);
        if (roboport.isEmpty() || roboport.get().getTeam().isEmpty())
            return;

        addRoboport(pos, roboport.get().getTeam().get(), serverWorld);
    }

    private static void onPointOfInterestRemoved(BlockPos pos, RegistryEntry<PointOfInterestType> type, ServerWorld serverWorld) {
        if (type.getKey().get() != AutomataPointOfInterestTypes.ROBOPORT)
            return;

        UUID removedTeamUuid = null;

        ChunkPos chunkPos = new ChunkPos(pos);
        for (UUID teamUuid : BotTeamPersistentState.getTeamMap().keySet()) {
            Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, teamUuid, serverWorld);
            if (serverNetwork.isEmpty())
                continue;

            removedTeamUuid = serverNetwork.get().teamUuid;
            break;
        }

        if (removedTeamUuid == null)
            return;

        removeRoboport(pos, removedTeamUuid, serverWorld);
    }

    private static void onRoboportTeamChanged(RoboportBlockEntity roboport, Optional<UUID> oldTeamUuid) {
        if (!(roboport.getWorld() instanceof ServerWorld serverWorld))
            return;

        if (oldTeamUuid.isPresent()) {
            removeRoboport(roboport.getPos(), oldTeamUuid.get(), serverWorld);
        }
        if (roboport.getTeam().isPresent()) {
            addRoboport(roboport.getPos(), roboport.getTeam().get(), serverWorld);
        }
    }
    //#endregion

    public static void initialize() {
        ServerChunkEvents.CHUNK_LOAD.register(BotNetworkManager::onChunkLoaded);
        ServerChunkEvents.CHUNK_UNLOAD.register(BotNetworkManager::onChunkUnloaded);
        ServerTickEvents.END_WORLD_TICK.register(BotNetworkManager::onEndWorldTick);
        PointOfInterestCallback.ADDED.register(BotNetworkManager::onPointOfInterestAdded);
        PointOfInterestCallback.REMOVED.register(BotNetworkManager::onPointOfInterestRemoved);
        RoboportBlockEntity.TEAM_CHANGED.register(BotNetworkManager::onRoboportTeamChanged);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            NETWORK_MAP_CACHE.clear();
            NETWORK_LOADER_CACHE.clear();
        });
    }
}
