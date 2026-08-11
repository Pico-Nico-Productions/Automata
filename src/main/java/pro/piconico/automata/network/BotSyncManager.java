package pro.piconico.automata.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobUtils;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;
import pro.piconico.automata.world.BotPersistentState;

public class BotSyncManager {
    private record SyncState(ServerWorld serverWorld, Set<BotJobAssignment> jobs) {
    }

    private static final Map<UUID, SyncState> subscribers = new HashMap<>();

    public static boolean isSubscribed(ServerPlayerEntity serverPlayer) {
        return subscribers.containsKey(serverPlayer.getUuid());
    }

    private static SyncState getCurrentState(ServerPlayerEntity serverPlayer) {
        ChunkBounds renderBounds = ChunkBounds.of(serverPlayer.getChunkPos(), serverPlayer.getViewDistance(), serverPlayer.getEntityWorld());

        Set<BotJobAssignment> currentJobs = new HashSet<>(BotPersistentState.getJobsIn(renderBounds, serverPlayer.getEntityWorld()));

        return new SyncState(serverPlayer.getEntityWorld(), currentJobs);
    }

    public static void subscribe(ServerPlayerEntity serverPlayer) {
        SyncState currentState = getCurrentState(serverPlayer);
        subscribers.put(serverPlayer.getUuid(), currentState);

        ServerPlayNetworking.send(serverPlayer, new BotSyncS2CPacket(Map.of(), BotJobUtils.map(currentState.jobs)));
    }

    public static void unsubscribe(ServerPlayerEntity serverPlayer) {
        ServerPlayNetworking.send(serverPlayer, BotSyncS2CPacket.CLEAR);

        subscribers.remove(serverPlayer.getUuid());
    }

    private static void syncTo(ServerPlayerEntity serverPlayer) {
        if (!isSubscribed(serverPlayer))
            return;

        SyncState oldState = subscribers.get(serverPlayer.getUuid());
        SyncState currentState = getCurrentState(serverPlayer);
        Set<BotJobAssignment> staleJobs;
        Set<BotJobAssignment> newJobs;

        boolean newWorld = currentState.serverWorld != oldState.serverWorld;
        if (newWorld) {
            staleJobs = Set.of();
            newJobs = currentState.jobs;
        }
        else {
            staleJobs = new HashSet<>(oldState.jobs);
            staleJobs.removeAll(currentState.jobs);
    
            newJobs = new HashSet<>(currentState.jobs);
            newJobs.removeAll(oldState.jobs);
        }

        subscribers.put(serverPlayer.getUuid(), currentState);

        ServerPlayNetworking.send(serverPlayer, new BotSyncS2CPacket(newWorld, BotJobUtils.map(staleJobs), BotJobUtils.map(newJobs)));
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
        BotPersistentState.JOBS_MUTATED.register((serverWorld, mutation) -> syncToSubscribers(serverWorld.getServer()));
    }
}
