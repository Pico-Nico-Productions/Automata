package pro.piconico.automata.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.bot.network.BotNetworkManager.ServerBotNetwork;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;
import pro.piconico.automata.world.BotPersistentState;

public class BotSyncManager {
    private record SyncState(Set<ServerBotNetwork> networks, Set<BotJobAssignment> jobs, ServerWorld serverWorld) {
        private static SyncState getCurrent(ServerPlayerEntity serverPlayer) {
            ServerWorld serverWorld = serverPlayer.getEntityWorld();
            ChunkBounds renderBounds = ChunkBounds.of(serverPlayer.getChunkPos(), serverPlayer.getViewDistance(), serverWorld);
            Set<ServerBotNetwork> currentNetworks = BotNetworkManager.getOrLoadNetworks(renderBounds, serverWorld);
            Set<BotJobAssignment> currentJobs = BotPersistentState.getJobsIn(renderBounds, serverWorld);

            return new SyncState(currentNetworks, currentJobs, serverWorld);
        }

        private BotSyncS2CPacket calculateDelta(SyncState newState) {
            boolean newWorld = newState.serverWorld != serverWorld;
            Set<ServerBotNetwork> obsoleteNetworks, mutatedNetworks;
            Set<BotJobAssignment> obsoleteJobAssignments, mutatedJobAssignments;

            if (newWorld) {
                obsoleteNetworks = Set.of();
                mutatedNetworks = newState.networks;

                obsoleteJobAssignments = Set.of();
                mutatedJobAssignments = newState.jobs;
            }
            else {
                obsoleteNetworks = new HashSet<>(networks);
                obsoleteNetworks.removeAll(newState.networks);
                mutatedNetworks = newState.networks.stream().filter(net -> !networks.contains(net) || net.isDirty()).collect(Collectors.toSet());
                for (ServerBotNetwork net : newState.networks) {
                    net.markNotDirty();
                }

                obsoleteJobAssignments = new HashSet<>(jobs);
                obsoleteJobAssignments.removeAll(newState.jobs);
                mutatedJobAssignments = new HashSet<>(newState.jobs);
                mutatedJobAssignments.removeAll(jobs);
            }

            return new BotSyncS2CPacket(newWorld, obsoleteNetworks, mutatedNetworks, obsoleteJobAssignments, mutatedJobAssignments);
        }
    }

    private static final Map<UUID, SyncState> subscribers = new HashMap<>();

    public static boolean isSubscribed(ServerPlayerEntity serverPlayer) {
        return subscribers.containsKey(serverPlayer.getUuid());
    }

    public static void subscribe(ServerPlayerEntity serverPlayer) {
        SyncState currentState = SyncState.getCurrent(serverPlayer);
        subscribers.put(serverPlayer.getUuid(), currentState);

        ServerPlayNetworking.send(serverPlayer, new BotSyncS2CPacket(Set.of(), currentState.networks, Set.of(), currentState.jobs));
    }

    public static void unsubscribe(ServerPlayerEntity serverPlayer) {
        ServerPlayNetworking.send(serverPlayer, BotSyncS2CPacket.CLEAR);

        subscribers.remove(serverPlayer.getUuid());
    }

    private static void syncTo(ServerPlayerEntity serverPlayer) {
        if (!isSubscribed(serverPlayer))
            return;

        SyncState oldState = subscribers.get(serverPlayer.getUuid());
        SyncState currentState = SyncState.getCurrent(serverPlayer);
        if (oldState.equals(currentState))
            return;

        subscribers.put(serverPlayer.getUuid(), currentState);
        BotSyncS2CPacket syncPacket = oldState.calculateDelta(currentState);
        ServerPlayNetworking.send(serverPlayer, syncPacket);
    }

    private static void syncToSubscribers(MinecraftServer server) {
        Iterator<UUID> subscriberIterator = subscribers.keySet().iterator();
        while (subscriberIterator.hasNext()) {
            UUID subscriberUuid = subscriberIterator.next();
            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(subscriberUuid);

            if (serverPlayer == null) {
                subscriberIterator.remove();
                continue;
            }

            syncTo(serverPlayer);
        }
    }

    // TODO: Sync on player movement

    public static void initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> unsubscribe(handler.getPlayer()));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((serverPlayer, oldServerWorld, newServerWorld) -> syncTo(serverPlayer));
        BotNetworkManager.NETWORKS_MUTATED.register((serverWorld, mutation) -> syncToSubscribers(serverWorld.getServer()));
        BotPersistentState.JOBS_MUTATED.register((serverWorld, mutation) -> syncToSubscribers(serverWorld.getServer()));
    }
}
