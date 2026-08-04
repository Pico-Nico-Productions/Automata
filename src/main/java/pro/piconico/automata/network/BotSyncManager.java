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
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobUtils;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;
import pro.piconico.automata.world.BotPersistentState;

public class BotSyncManager {
    // TODO: Add world
    private record SyncState(Set<BotJobAssignment> jobs) {
    }

    private static final Map<UUID, SyncState> subscribers = new HashMap<>();

    private static SyncState getCurrentState(ServerPlayerEntity serverPlayer) {
        ChunkBounds renderBounds = ChunkBounds.of(serverPlayer.getChunkPos(), serverPlayer.getViewDistance(), serverPlayer.getEntityWorld());
        Set<BotJobAssignment> currentJobs = new HashSet<>(BotPersistentState.getJobsIn(renderBounds, serverPlayer.getEntityWorld()));

        return new SyncState(currentJobs);
    }

    public static void subscribe(ServerPlayerEntity serverPlayer) {
        SyncState currentState = getCurrentState(serverPlayer);
        subscribers.put(serverPlayer.getUuid(), currentState);

        ServerPlayNetworking.send(serverPlayer, new BotSyncS2CPacket(BotJobUtils.map(currentState.jobs), Map.of()));
    }

    public static void unsubscribe(ServerPlayerEntity serverPlayer) {
        subscribers.remove(serverPlayer.getUuid());
    }

    public static boolean isSubscribed(ServerPlayerEntity serverPlayer) {
        return subscribers.containsKey(serverPlayer.getUuid());
    }

    private static void syncTo(ServerPlayerEntity serverPlayer) {
        if (!isSubscribed(serverPlayer))
            return;

        SyncState oldState = subscribers.get(serverPlayer.getUuid());
        SyncState currentState = getCurrentState(serverPlayer);

        Set<BotJobAssignment> newJobs = new HashSet<>(currentState.jobs());
        newJobs.removeAll(oldState.jobs());

        Set<BotJobAssignment> staleJobs = new HashSet<>(oldState.jobs());
        staleJobs.removeAll(currentState.jobs()); 

        subscribers.put(serverPlayer.getUuid(), currentState);

        ServerPlayNetworking.send(serverPlayer, new BotSyncS2CPacket(BotJobUtils.map(newJobs), BotJobUtils.map(staleJobs)));
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
