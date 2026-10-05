package pro.piconico.automata.bot.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.poi.PointOfInterestType;
import pro.piconico.automata.Automata;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.bot.device.LogisticStorage;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.event.PointOfInterestCallback;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.util.MapUtils;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;
import pro.piconico.automata.world.BotTeamPersistentState;

public class BotNetworkManager {
    private static final Map<ServerWorld, Map<UUID, BotNetworkMap<ServerBotNetwork>>> NETWORK_MAP_CACHE = new HashMap<>();
    private static final Map<ServerWorld, Map<UUID, BotNetworkLoader>> NETWORK_LOADER_CACHE = new HashMap<>();
    private static final int DEFAULT_CHUNK_RADIUS = 0;

    public enum Mutation {
        ADD, REMOVE
    }

    @FunctionalInterface
    public interface Mutate {
        void onMutate(ServerWorld serverWorld, UUID teamUuid, Mutation mutation);
    }

    public static final Event<Mutate> NETWORKS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (serverWorld, teamUuid, mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(serverWorld, teamUuid, mutation);
        }
    });

    //#region Requests
    private static Optional<ServerBotNetwork> getNetwork(ServerWorld serverWorld, UUID teamUuid, ChunkPos chunkPos) {
        return MapUtils.getNested(NETWORK_MAP_CACHE, serverWorld, teamUuid, chunkPos);
    }

    public static Optional<BotNetwork> getNetworkCopy(ServerWorld serverWorld, UUID teamUuid, ChunkPos chunkPos) {
        return getNetwork(serverWorld, teamUuid, chunkPos).map(network -> new ServerBotNetwork(network));
    }

    public static Set<BotNetwork> getNetworkCopies(ServerWorld serverWorld, UUID teamUuid, ChunkBounds chunkBounds) {
        Set<BotNetwork> networks = new HashSet<>();

        Set<ChunkPos> networkChunks = new HashSet<>();
        for (ChunkPos chunkPos : chunkBounds.toStream().toList()) {
            if (networkChunks.contains(chunkPos))
                continue;

            Optional<BotNetwork> serverNetwork = getNetworkCopy(serverWorld, teamUuid, chunkPos);
            if (serverNetwork.isEmpty())
                continue;

            networks.add(serverNetwork.get());
            networkChunks.addAll(serverNetwork.get().roboportMap.keySet());
        }

        return networks;
    }

    public static Stream<RoboportBlockEntity> streamRoboportsNear(ServerWorld serverWorld, UUID teamUuid, BlockPos blockPos, int chunkRadius) {
        return ChunkPos.stream(new ChunkPos(blockPos), chunkRadius) //
                .map(chunkPos -> getNetwork(serverWorld, teamUuid, chunkPos).orElse(null)).filter(net -> net != null).distinct() //
                .flatMap(ServerBotNetwork::streamRoboports).map(pos -> serverWorld.getBlockEntity(pos, AutomataEntities.ROBOPORT).orElse(null))
                .filter(port -> port != null);
    }

    public static Stream<RoboportBlockEntity> streamRoboportsNear(ServerWorld serverWorld, UUID teamUuid, BlockPos blockPos) {
        return streamRoboportsNear(serverWorld, teamUuid, blockPos, DEFAULT_CHUNK_RADIUS);
    }

    public static Stream<LogisticStorage<?>> streamLogisticStoragesNear(ServerWorld serverWorld, UUID teamUuid, BlockPos blockPos, int chunkRadius) {
        return ChunkPos.stream(new ChunkPos(blockPos), chunkRadius) //
                .map(chunkPos -> getNetwork(serverWorld, teamUuid, chunkPos).orElse(null)).filter(net -> net != null).distinct() //
                .flatMap(ServerBotNetwork::streamLogisticStorages)
                .<LogisticStorage<?>>map(pos -> serverWorld.getBlockEntity(pos, AutomataEntities.LOGISTIC_CHEST).orElse(null))
                .filter(storage -> storage != null);
    }

    public static Stream<LogisticStorage<?>> streamLogisticStoragesNear(ServerWorld serverWorld, UUID teamUuid, BlockPos blockPos) {
        return streamLogisticStoragesNear(serverWorld, teamUuid, blockPos, DEFAULT_CHUNK_RADIUS);
    }

    public static Stream<LogisticStorage<?>> streamReservationLogisticStorages(ServerWorld serverWorld, UUID teamUuid, BlockPos pos, UUID entityUuid) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(serverWorld, teamUuid, new ChunkPos(pos));

        if (serverNetwork.isEmpty())
            return Stream.empty();

        return serverNetwork.get().getReservations(entityUuid).keySet().stream().map(serverNetwork.get()::getLogisticStorage);
    }

    public static Map<BlockPos, Map<Integer, Integer>> getReservations(ServerWorld serverWorld, UUID teamUuid, BlockPos pos, UUID entityUuid) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(serverWorld, teamUuid, new ChunkPos(pos));

        if (serverNetwork.isEmpty())
            return Map.of();

        return serverNetwork.get().getReservations(entityUuid);
    }

    public static Optional<BotEntity> getOrSpawnBotFor(ServerWorld serverWorld, UUID teamUuid, BotJob job) {
        if (!job.canStart(serverWorld))
            return Optional.empty();

        Optional<ServerBotNetwork> serverNetwork = getNetwork(serverWorld, teamUuid, new ChunkPos(job.pos()));

        if (serverNetwork.isEmpty())
            return Optional.empty();

        return serverNetwork.get().getOrSpawnBotFor(job);
    }
    //#endregion

    //#region Cache Mutation Operations
    private static void putNetwork(ServerBotNetwork serverNetwork) {
        Map<UUID, BotNetworkMap<ServerBotNetwork>> teamMap = NETWORK_MAP_CACHE.computeIfAbsent(serverNetwork.serverWorld, serverWorld -> new HashMap<>());
        BotNetworkMap<ServerBotNetwork> networkMap = teamMap.computeIfAbsent(serverNetwork.teamUuid, uuid -> new BotNetworkMap<>());
        for (ChunkPos chunkPos : serverNetwork.roboportMap.keySet()) {
            networkMap.put(chunkPos, serverNetwork);
            serverNetwork.serverWorld.getChunkManager().addTicket(ChunkTicketType.PLAYER_LOADING, chunkPos, 0);
        }
    }

    private static void loadNetwork(ServerWorld serverWorld, UUID teamUuid, WorldChunk chunk) {
        Optional<BotNetworkLoader> networkLoader = MapUtils.getNested(NETWORK_LOADER_CACHE, serverWorld, teamUuid);
        if (networkLoader.isPresent()) {
            networkLoader.get().load(chunk);
            return;
        }

        BotNetworkLoader newNetworkLoader = new BotNetworkLoader(serverWorld, teamUuid, chunk);
        if (newNetworkLoader.isComplete())
            return;

        Map<UUID, BotNetworkLoader> teamMap = NETWORK_LOADER_CACHE.computeIfAbsent(serverWorld, s -> new HashMap<>());
        teamMap.put(teamUuid, newNetworkLoader);
    }

    private static void cacheLoadedNetwork(BotNetworkLoader networkLoader) {
        Set<ServerBotNetwork> serverNetworks = new ServerBotNetwork(networkLoader.getRoboports(), networkLoader.getLogisticStorages(), networkLoader.teamUuid,
                networkLoader.serverWorld).getSubnetworks();
        for (ServerBotNetwork serverNetwork : serverNetworks) {
            putNetwork(serverNetwork);
        }
        MapUtils.<BotNetworkLoader>removeNested(NETWORK_LOADER_CACHE, networkLoader.serverWorld, networkLoader.teamUuid);

        NETWORKS_MUTATED.invoker().onMutate(networkLoader.serverWorld, networkLoader.teamUuid, Mutation.ADD);
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

        NETWORKS_MUTATED.invoker().onMutate(serverNetwork.serverWorld, serverNetwork.teamUuid, Mutation.REMOVE);
    }

    private static void addRoboport(ServerWorld serverWorld, UUID teamUuid, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);

        Optional<ServerBotNetwork> serverNetwork = getNetwork(serverWorld, teamUuid, chunkPos);
        if (serverNetwork.isPresent()) {
            serverNetwork.get().addRoboport(pos);
            NETWORKS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.ADD);
            return;
        }

        WorldChunk worldChunk = serverWorld.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z);

        if (worldChunk == null)
            return;

        loadNetwork(serverWorld, teamUuid, worldChunk);
    }

    private static void removeRoboport(ServerWorld serverWorld, UUID teamUuid, BlockPos pos) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(serverWorld, teamUuid, new ChunkPos(pos));

        if (serverNetwork.isEmpty())
            return;

        ServerBotNetwork.RemovedObjects removedObjects = serverNetwork.get().removeRoboport(pos);

        if (removedObjects == ServerBotNetwork.RemovedObjects.NONE)
            return;

        removedObjects.chunk().ifPresent(chunk -> {
            MapUtils.<ServerBotNetwork>removeNested(NETWORK_MAP_CACHE, serverWorld, teamUuid, chunk);
        });
        for (ServerBotNetwork subnetwork : removedObjects.subnetworks()) {
            putNetwork(subnetwork);
        }

        NETWORKS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.REMOVE);
    }

    private static void addLogisticStorage(ServerWorld serverWorld, UUID teamUuid, BlockPos pos) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(serverWorld, teamUuid, new ChunkPos(pos));
        if (serverNetwork.isEmpty())
            return;

        serverNetwork.get().addLogisticStorage(pos);
        NETWORKS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.ADD);
    }

    private static void removeLogisticStorage(ServerWorld serverWorld, UUID teamUuid, BlockPos pos) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(serverWorld, teamUuid, new ChunkPos(pos));

        if (serverNetwork.isEmpty() || !serverNetwork.get().removeLogisticStorage(pos))
            return;

        NETWORKS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.REMOVE);
    }
    //#endregion

    //#region Event Listeners
    private static void onChunkLoaded(ServerWorld serverWorld, WorldChunk chunk) {
        for (UUID teamUuid : BotTeamPersistentState.getTeamUuids()) {
            loadNetwork(serverWorld, teamUuid, chunk);
        }
    }

    private static void onChunkUnloaded(ServerWorld serverWorld, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        for (UUID teamUuid : BotTeamPersistentState.getTeamUuids()) {
            Optional<ServerBotNetwork> serverNetwork = getNetwork(serverWorld, teamUuid, chunkPos);

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

        for (UUID teamUuid : BotTeamPersistentState.getTeamUuids()) {
            Optional<BotNetworkLoader> networkLoader = MapUtils.getNested(NETWORK_LOADER_CACHE, serverWorld, teamUuid);
            if (networkLoader.isEmpty())
                continue;

            networkLoader.get().process();

            if (!networkLoader.get().isComplete())
                continue;

            cacheLoadedNetwork(networkLoader.get());
        }
    }

    private static void onPointOfInterestAdded(ServerWorld serverWorld, BlockPos pos, Optional<BlockEntity> blockEntity,
            RegistryEntry<PointOfInterestType> pointOfInterestType) {
        if (blockEntity.isEmpty() || !(blockEntity.get() instanceof BotDevice<?> device) || device.getTeamUuid().isEmpty())
            return;

        UUID teamUuid = device.getTeamUuid().get();
        switch (device) {
        case RoboportBlockEntity ignored -> addRoboport(serverWorld, teamUuid, pos);
        case LogisticStorage<?> ignored -> addLogisticStorage(serverWorld, teamUuid, pos);
        default -> {
        }
        }
    }

    private static void onPointOfInterestRemoved(ServerWorld serverWorld, BlockPos pos, Optional<BlockEntity> blockEntity,
            RegistryEntry<PointOfInterestType> pointOfInterestType) {
        if (blockEntity.isEmpty() || !(blockEntity.get() instanceof BotDevice<?> device) || device.getTeamUuid().isEmpty())
            return;

        UUID teamUuid = device.getTeamUuid().get();
        switch (device) {
        case RoboportBlockEntity ignored -> removeRoboport(serverWorld, teamUuid, pos);
        case LogisticStorage<?> ignored -> removeLogisticStorage(serverWorld, teamUuid, pos);
        default -> {
        }
        }
    }

    public static void unreserve(ServerWorld serverWorld, UUID teamUuid, BlockPos pos, int slot, int count, UUID entityUuid) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(serverWorld, teamUuid, new ChunkPos(pos));

        if (serverNetwork.isEmpty())
            return;

        serverNetwork.get().unreserve(entityUuid, pos, slot, count);
    }

    private static void onTeamsMutated(MinecraftServer server, BotTeam team, BotTeamPersistentState.Mutation mutation) {
        if (mutation != BotTeamPersistentState.Mutation.REMOVE)
            return;

        for (ServerWorld serverWorld : Set.copyOf(NETWORK_MAP_CACHE.keySet())) {
            Optional<BotNetworkMap<ServerBotNetwork>> networkMap = MapUtils.removeNested(NETWORK_MAP_CACHE, serverWorld, team.UUID);

            if (networkMap.isEmpty())
                return;

            for (ServerBotNetwork serverNetwork : networkMap.get().values()) {
                serverNetwork.streamRoboports().forEach(pos -> {
                    Optional<RoboportBlockEntity> roboport = serverWorld.getBlockEntity(pos, AutomataEntities.ROBOPORT);

                    if (roboport.isEmpty()) {
                        Automata.logError(BotNetworkManager.class.getSimpleName() + " had an invalid roboport " + BlockPos.class.getSimpleName(),
                                IllegalStateException::new);
                        return;
                    }

                    roboport.get().setTeamUuid(Optional.empty());
                });

                serverNetwork.streamLogisticStorages().forEach(pos -> {
                    BlockEntity blockEntity = serverWorld.getBlockEntity(pos);

                    if (!(blockEntity instanceof LogisticStorage<?> logisticStorage)) {
                        Automata.logError(BotNetworkManager.class.getSimpleName() + " had an invalid logistic storage " + BlockPos.class.getSimpleName(),
                                IllegalStateException::new);
                        return;
                    }

                    logisticStorage.setTeamUuid(Optional.empty());
                });
            }

            for (Entity entity : serverWorld.iterateEntities()) {
                if (!(entity instanceof BotEntity botEntity) || botEntity.getTeamUuid().filter(team.UUID::equals).isEmpty())
                    continue;

                botEntity.setTeamUuid(Optional.empty());
            }

            NETWORKS_MUTATED.invoker().onMutate(serverWorld, team.UUID, Mutation.REMOVE);
        }
    }

    private static void onTeamChanged(World world, BotDevice<?> device, Optional<UUID> oldTeamUuid) {
        if (!(world instanceof ServerWorld serverWorld) || !(device instanceof BlockBotDevice blockDevice))
            return;

        switch (device) {
        case RoboportBlockEntity roboport:
            oldTeamUuid.ifPresent(uuid -> removeRoboport(serverWorld, uuid, roboport.getPos()));
            roboport.getTeamUuid().ifPresent(uuid -> addRoboport(serverWorld, uuid, roboport.getPos()));
            break;
        case LogisticStorage<?> logisticStorage:
            oldTeamUuid.ifPresent(uuid -> removeLogisticStorage(serverWorld, uuid, blockDevice.getPos()));
            logisticStorage.getTeamUuid().ifPresent(uuid -> addLogisticStorage(serverWorld, uuid, blockDevice.getPos()));
            break;
        default:
            break;
        }
    }
    //#endregion

    public static void initialize() {
        ServerChunkEvents.CHUNK_LOAD.register(BotNetworkManager::onChunkLoaded);
        ServerChunkEvents.CHUNK_UNLOAD.register(BotNetworkManager::onChunkUnloaded);
        ServerTickEvents.END_WORLD_TICK.register(BotNetworkManager::onEndWorldTick);
        PointOfInterestCallback.ADDED.register(BotNetworkManager::onPointOfInterestAdded);
        PointOfInterestCallback.REMOVED.register(BotNetworkManager::onPointOfInterestRemoved);
        BotTeamPersistentState.TEAMS_MUTATED.register(BotNetworkManager::onTeamsMutated);
        BotDevice.TEAM_CHANGED_CALLBACKS.add(BotNetworkManager::onTeamChanged);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            NETWORK_MAP_CACHE.clear();
            NETWORK_LOADER_CACHE.clear();
        });
    }
}
