package pro.piconico.automata.network.sync;

import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.network.packet.BotCacheS2CPacket;
import pro.piconico.automata.registry.AutomataPersistentStates;
import pro.piconico.automata.world.BotPersistentState;

public class BotCacheSynchronizer {
    private static void syncToAll(ServerWorld world) {
        Set<BlockPos> roboports = AutomataPersistentStates.get(world, AutomataPersistentStates.BOT_PERSISTENT_STATE).getRoboports();
        BotCacheS2CPacket packet = new BotCacheS2CPacket(List.copyOf(roboports));
        for (ServerPlayerEntity player : (world).getPlayers()) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    private static void syncToPlayer(ServerPlayerEntity player) {
        Set<BlockPos> roboports = AutomataPersistentStates.get(player.getEntityWorld(), AutomataPersistentStates.BOT_PERSISTENT_STATE).getRoboports();
        ServerPlayNetworking.send(player, new BotCacheS2CPacket(List.copyOf(roboports)));
    }

    public static void initialize() {
        BotPersistentState.ROBOPORTS_MUTATE.register((serverWorld) -> syncToAll(serverWorld));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncToPlayer(handler.getPlayer()));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, originWorld, destinationWorld) -> syncToPlayer(player));
    }
}
