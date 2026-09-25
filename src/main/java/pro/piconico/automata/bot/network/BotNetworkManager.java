package pro.piconico.automata.bot.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
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
        void onMutate(ServerWorld serverWorld, UUID teamUuid, Mutation mutation);
    }

    public static final Event<Mutate> NETWORKS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (teamUuid, serverWorld, mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(teamUuid, serverWorld, mutation);
        }
    });

    //#region Network Getting
    private static Optional<ServerBotNetwork> getNetwork(ChunkPos chunkPos, UUID teamUuid, ServerWorld serverWorld) {
        return MapUtils.getNested(NETWORK_MAP_CACHE, serverWorld, teamUuid, chunkPos);
    }

    public static Optional<BotNetwork> getNetworkCopy(ChunkPos chunkPos, UUID teamUuid, ServerWorld serverWorld) {
        return getNetwork(chunkPos, teamUuid, serverWorld).map(network -> new ServerBotNetwork(network));
    }

    public static Set<BotNetwork> getNetworkCopies(ChunkBounds chunkBounds, UUID teamUuid, ServerWorld serverWorld) {
        Set<BotNetwork> networks = new HashSet<>();

        Set<ChunkPos> networkChunks = new HashSet<>();
        for (ChunkPos chunkPos : chunkBounds.toStream().toList()) {
            if (networkChunks.contains(chunkPos))
                continue;

            Optional<BotNetwork> serverNetwork = getNetworkCopy(chunkPos, teamUuid, serverWorld);
            if (serverNetwork.isEmpty())
                continue;

            networks.add(serverNetwork.get());
            networkChunks.addAll(serverNetwork.get().roboportMap.keySet());
        }

        return networks;
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
        Set<ServerBotNetwork> serverNetworks = new ServerBotNetwork(networkLoader.getRoboports(), networkLoader.getLogisticStorages(), networkLoader.teamUuid,
                networkLoader.serverWorld).getSubnetworks();
        for (ServerBotNetwork serverNetwork : serverNetworks) {
            putNetwork(serverNetwork);
        }
        MapUtils.<NetworkLoader>removeNested(NETWORK_LOADER_CACHE, networkLoader.serverWorld, networkLoader.teamUuid);

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

    private static void addRoboport(BlockPos pos, UUID teamUuid, ServerWorld serverWorld) {
        ChunkPos chunkPos = new ChunkPos(pos);

        Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, teamUuid, serverWorld);
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

    private static void removeRoboport(BlockPos pos, UUID teamUuid, ServerWorld serverWorld) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(new ChunkPos(pos), teamUuid, serverWorld);

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

    private static void addLogisticStorage(BlockPos pos, UUID teamUuid, ServerWorld serverWorld) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(new ChunkPos(pos), teamUuid, serverWorld);
        if (serverNetwork.isEmpty())
            return;

        serverNetwork.get().addLogisticStorage(pos);
        NETWORKS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.ADD);
    }

    private static void removeLogisticStorage(BlockPos pos, UUID teamUuid, ServerWorld serverWorld) {
        Optional<ServerBotNetwork> serverNetwork = getNetwork(new ChunkPos(pos), teamUuid, serverWorld);

        if (serverNetwork.isEmpty() || !serverNetwork.get().removeLogisticStorage(pos))
            return;

        NETWORKS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.REMOVE);
    }

    public static Optional<BotEntity> getOrSpawnBotFor(ServerWorld serverWorld, UUID teamUuid, BotJob job) {
        ChunkPos chunkPos = new ChunkPos(job.pos());

        Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, teamUuid, serverWorld);
        if (serverNetwork.isEmpty())
            return Optional.empty();

        return serverNetwork.get().getOrSpawnBotFor(job);
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

        for (UUID teamUuid : BotTeamPersistentState.getTeamUuids()) {
            Optional<NetworkLoader> networkLoader = MapUtils.getNested(NETWORK_LOADER_CACHE, serverWorld, teamUuid);
            if (networkLoader.isEmpty())
                continue;

            networkLoader.get().process();

            if (!networkLoader.get().isComplete())
                continue;

            cacheLoadedNetwork(networkLoader.get());
        }
    }

    private static void onPointOfInterestAdded(ServerWorld serverWorld, BlockPos pos, RegistryEntry<PointOfInterestType> pointOfInterestType) {
        BlockEntity blockEntity = serverWorld.getBlockEntity(pos);
        if (blockEntity == null || !(blockEntity instanceof BotDevice<?> device) || device.getTeamUuid().isEmpty())
            return;

        UUID teamUuid = device.getTeamUuid().get();
        switch (device) {
        case RoboportBlockEntity ignored -> addRoboport(pos, teamUuid, serverWorld);
        case LogisticStorage<?> ignored -> addLogisticStorage(pos, teamUuid, serverWorld);
        default -> {
        }
        }
    }

    private static void onPointOfInterestRemoved(ServerWorld serverWorld, BlockPos pos, RegistryEntry<PointOfInterestType> pointOfInterestType) {
        RegistryKey<PointOfInterestType> type = pointOfInterestType.getKey().get();

        if (type != AutomataPointOfInterestTypes.ROBOPORT && type != AutomataPointOfInterestTypes.LOGISTIC_CHEST)
            return;

        UUID removedTeamUuid = null;

        ChunkPos chunkPos = new ChunkPos(pos);
        for (UUID teamUuid : BotTeamPersistentState.getTeamUuids()) {
            Optional<ServerBotNetwork> serverNetwork = getNetwork(chunkPos, teamUuid, serverWorld);
            if (serverNetwork.isEmpty())
                continue;

            removedTeamUuid = serverNetwork.get().teamUuid;
            break;
        }

        if (removedTeamUuid == null)
            return;

        if (type == AutomataPointOfInterestTypes.ROBOPORT) {
            removeRoboport(pos, removedTeamUuid, serverWorld);
        }
        else if (type == AutomataPointOfInterestTypes.LOGISTIC_CHEST) {
            removeLogisticStorage(pos, removedTeamUuid, serverWorld);
        }
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
            oldTeamUuid.ifPresent(uuid -> removeRoboport(roboport.getPos(), uuid, serverWorld));
            roboport.getTeamUuid().ifPresent(uuid -> addRoboport(roboport.getPos(), uuid, serverWorld));
            break;
        case LogisticStorage<?> logisticStorage:
            oldTeamUuid.ifPresent(uuid -> removeLogisticStorage(blockDevice.getPos(), uuid, serverWorld));
            logisticStorage.getTeamUuid().ifPresent(uuid -> addLogisticStorage(blockDevice.getPos(), uuid, serverWorld));
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
