package pro.piconico.automata.network;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import pro.piconico.automata.bot.job.BotJobAssignmentMap;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.bot.network.BotNetworkManager.ServerBotNetwork;
import pro.piconico.automata.bot.network.BotNetworkMap;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;
import pro.piconico.automata.world.BotJobPersistentState;

public class BotSyncManager {
    private record SyncState(BotNetworkMap<ServerBotNetwork> networkMap, BotJobAssignmentMap jobAssignmentMap, ServerWorld serverWorld) {
        private static SyncState getCurrent(ServerPlayerEntity serverPlayer) {
            ServerWorld serverWorld = serverPlayer.getEntityWorld();
            ChunkBounds renderBounds = ChunkBounds.of(serverPlayer.getChunkPos(), serverPlayer.getViewDistance(), serverWorld);
            // TODO: Make BotJobAssignment and BotNetwork implement Cloneable so a unique snapshot can be created and BotNetwork.dirty is obsolete
            BotNetworkMap<ServerBotNetwork> currentNetworkMap = new BotNetworkMap<>(BotNetworkManager.getNetworks(renderBounds, serverWorld));
            BotJobAssignmentMap currentJobMap = new BotJobAssignmentMap(BotJobPersistentState.getJobs(renderBounds, serverWorld));

            return new SyncState(currentNetworkMap, currentJobMap, serverWorld);
        }

        private BotSyncS2CPacket calculateDelta(SyncState newState) {
            boolean newWorld = newState.serverWorld != serverWorld;
            BotNetworkMap<ServerBotNetwork> deltaNetworkMap;
            BotJobAssignmentMap deltaJobAssignmentMap;

            if (newWorld) {
                deltaNetworkMap = new BotNetworkMap<>(newState.networkMap);
                deltaJobAssignmentMap = new BotJobAssignmentMap(newState.jobAssignmentMap);
            }
            else {
                deltaNetworkMap = networkMap.calculateDelta(newState.networkMap);
                deltaJobAssignmentMap = jobAssignmentMap.calculateDelta(newState.jobAssignmentMap);
            }

            return new BotSyncS2CPacket(newWorld, deltaNetworkMap, deltaJobAssignmentMap);
        }
    }

    private static final Map<UUID, SyncState> subscribers = new HashMap<>();

    public static boolean isSubscribed(ServerPlayerEntity serverPlayer) {
        return subscribers.containsKey(serverPlayer.getUuid());
    }

    public static void subscribe(ServerPlayerEntity serverPlayer) {
        SyncState currentState = SyncState.getCurrent(serverPlayer);
        subscribers.put(serverPlayer.getUuid(), currentState);

        ServerPlayNetworking.send(serverPlayer, new BotSyncS2CPacket(currentState.networkMap, currentState.jobAssignmentMap));
    }

    public static void unsubscribe(ServerPlayerEntity serverPlayer) {
        subscribers.remove(serverPlayer.getUuid());

        ServerPlayNetworking.send(serverPlayer, BotSyncS2CPacket.CLEAR);
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

    private static void syncToSubscribers(ServerWorld serverWorld) {
        Iterator<UUID> subscriberIterator = subscribers.keySet().iterator();
        while (subscriberIterator.hasNext()) {
            UUID subscriberUuid = subscriberIterator.next();
            ServerPlayerEntity serverPlayer = serverWorld.getServer().getPlayerManager().getPlayer(subscriberUuid);
            if (serverPlayer == null) {
                subscriberIterator.remove();
                continue;
            }
            
            if (serverPlayer.getEntityWorld() != serverWorld)
                continue;

            syncTo(serverPlayer);
        }

        BotNetworkManager.markAllNotDirty(serverWorld);
    }

    public static void initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> unsubscribe(handler.getPlayer()));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((serverPlayer, oldServerWorld, newServerWorld) -> syncTo(serverPlayer));
        BotNetworkManager.NETWORKS_MUTATED.register((serverWorld, mutation) -> syncToSubscribers(serverWorld));
        BotJobPersistentState.JOBS_MUTATED.register((serverWorld, mutation) -> syncToSubscribers(serverWorld));
    }
}
