package pro.piconico.automata.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.bot.job.BotJobAssignmentMap;
import pro.piconico.automata.bot.network.BotNetwork;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.bot.network.BotNetworkMap;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.component.TeamComponent;
import pro.piconico.automata.entity.LivingEntityUtils;
import pro.piconico.automata.event.HoldItemCallback;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;
import pro.piconico.automata.world.BotJobPersistentState;
import pro.piconico.automata.world.BotTeamPersistentState;
import pro.piconico.automata.world.BotTeamPersistentState.Mutation;

public class BotSyncManager {
    private record SyncState(ServerWorld serverWorld, UUID teamUuid, BotNetworkMap<BotNetwork> networkMap, BotJobAssignmentMap jobAssignmentMap) {
        private static SyncState getCurrent(ServerPlayerEntity serverPlayer, UUID teamUuid) {
            ServerWorld serverWorld = serverPlayer.getEntityWorld();
            ChunkBounds renderBounds = ChunkBounds.of(serverPlayer.getChunkPos(), serverPlayer.getViewDistance(), serverWorld);
            BotNetworkMap<BotNetwork> currentNetworkMap = new BotNetworkMap<>(BotNetworkManager.getNetworkCopies(serverWorld, teamUuid, renderBounds));
            BotJobAssignmentMap currentJobMap = new BotJobAssignmentMap(BotJobPersistentState.getJobAssignmentCopies(serverWorld, teamUuid, renderBounds));

            return new SyncState(serverWorld, teamUuid, currentNetworkMap, currentJobMap);
        }

        private BotSyncS2CPacket calculateDelta(SyncState newState) {
            boolean newWorld = newState.serverWorld != serverWorld;
            BotNetworkMap<BotNetwork> deltaNetworkMap;
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

    private static boolean isSubscribed(ServerPlayerEntity serverPlayer) {
        return subscribers.containsKey(serverPlayer.getUuid());
    }

    private static boolean isSubscribed(ServerPlayerEntity serverPlayer, UUID teamUuid) {
        return isSubscribed(serverPlayer) && subscribers.get(serverPlayer.getUuid()).teamUuid.equals(teamUuid);
    }

    private static void subscribe(ServerPlayerEntity serverPlayer, UUID teamUuid) {
        UUID playerUuid = serverPlayer.getUuid();

        if (isSubscribed(serverPlayer, teamUuid))
            return;

        SyncState currentState = SyncState.getCurrent(serverPlayer, teamUuid);
        subscribers.put(playerUuid, currentState);

        ServerPlayNetworking.send(serverPlayer, new BotSyncS2CPacket(true, currentState.networkMap, currentState.jobAssignmentMap));
    }

    private static void unsubscribe(ServerPlayerEntity serverPlayer) {
        if (!subscribers.containsKey(serverPlayer.getUuid()))
            return;

        subscribers.remove(serverPlayer.getUuid());

        ServerPlayNetworking.send(serverPlayer, BotSyncS2CPacket.CLEAR);
    }

    private static void updateSubscription(ServerPlayerEntity serverPlayer, Optional<UUID> teamUuid) {
        if (teamUuid.filter(uuid -> BotTeamPersistentState.teamExistsAndHasMemberWithRead(uuid, serverPlayer)).isEmpty()) {
            unsubscribe(serverPlayer);
            return;
        }

        subscribe(serverPlayer, teamUuid.get());
    }

    private static void syncTo(ServerPlayerEntity serverPlayer) {
        if (!isSubscribed(serverPlayer))
            return;

        SyncState oldState = subscribers.get(serverPlayer.getUuid());
        SyncState currentState = SyncState.getCurrent(serverPlayer, oldState.teamUuid);
        if (oldState.equals(currentState))
            return;

        subscribers.put(serverPlayer.getUuid(), currentState);
        BotSyncS2CPacket syncPacket = oldState.calculateDelta(currentState);
        ServerPlayNetworking.send(serverPlayer, syncPacket);
    }

    private static void syncToSubscribers(ServerWorld serverWorld, UUID teamUuid) {
        Iterator<UUID> subscriberIterator = subscribers.keySet().iterator();
        while (subscriberIterator.hasNext()) {
            UUID subscriberUuid = subscriberIterator.next();
            if (!subscribers.get(subscriberUuid).teamUuid.equals(teamUuid))
                continue;

            ServerPlayerEntity serverPlayer = serverWorld.getServer().getPlayerManager().getPlayer(subscriberUuid);

            if (serverPlayer == null) {
                subscriberIterator.remove();
                continue;
            }

            if (serverPlayer.getEntityWorld() != serverWorld)
                continue;

            syncTo(serverPlayer);
        }
    }

    private static void onTeamsMutated(MinecraftServer server, BotTeam team, Mutation mutation) {
        if (!(mutation == Mutation.MEMBERS || mutation == Mutation.REMOVE))
            return;

        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            updateSubscription(serverPlayer, Optional.of(team.uuid));
        }
    }

    private static void onHoldStarted(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return;

        Optional<UUID> teamUuid = TeamComponent.get(stack).filter(uuid -> BotTeamPersistentState.teamExistsAndHasMemberWithRead(uuid, serverPlayer));

        if (teamUuid.isEmpty())
            return;

        updateSubscription(serverPlayer, teamUuid);
    }

    private static void onHoldEnded(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!(player instanceof ServerPlayerEntity serverPlayer) || TeamComponent.get(stack).isEmpty())
            return;

        Collection<ItemStack> heldStacks = LivingEntityUtils.getHeldStacks(player, s -> TeamComponent.get(s).isPresent()).values();
        Optional<UUID> teamUuid = heldStacks.stream().map(heldStack -> TeamComponent.get(heldStack).orElse(null)).filter(uuid -> uuid != null).findFirst();
        updateSubscription(serverPlayer, teamUuid);
    }

    private static void onTeamChanged(World world, BotDevice<?> device, Optional<UUID> oldTeamUuid) {
        if (!(device instanceof ItemBotDevice itemDevice) || !(itemDevice.player instanceof ServerPlayerEntity serverPlayer))
            return;

        updateSubscription(serverPlayer, device.getTeamUuid());
    }

    public static void initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> unsubscribe(handler.getPlayer()));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((serverPlayer, oldServerWorld, newServerWorld) -> syncTo(serverPlayer));
        BotTeamPersistentState.TEAMS_MUTATED.register(BotSyncManager::onTeamsMutated);
        BotNetworkManager.NETWORKS_MUTATED.register((serverWorld, teamUuid, mutation) -> syncToSubscribers(serverWorld, teamUuid));
        BotJobPersistentState.JOBS_MUTATED.register((serverWorld, teamUuid, mutation) -> syncToSubscribers(serverWorld, teamUuid));
        HoldItemCallback.HOLD_STARTED.register(BotSyncManager::onHoldStarted);
        HoldItemCallback.HOLD_ENDED.register(BotSyncManager::onHoldEnded);
        BotDevice.TEAM_CHANGED_CALLBACKS.add(BotSyncManager::onTeamChanged);
    }
}
