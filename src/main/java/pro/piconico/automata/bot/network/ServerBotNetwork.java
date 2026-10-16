package pro.piconico.automata.bot.network;

import java.util.ArrayList;
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
import java.util.UUID;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.device.LogisticStorage;
import pro.piconico.automata.bot.device.LogisticStorage.Reservation;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.item.ItemUtils.PredicateItemStack;
import pro.piconico.automata.registry.AutomataBots;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

public class ServerBotNetwork extends BotNetwork {
    final ServerWorld serverWorld;

    ServerBotNetwork(Collection<BlockPos> roboports, Collection<BlockPos> logisticStorages, UUID teamUuid, ServerWorld serverWorld) {
        super(roboports, logisticStorages, teamUuid);
        this.serverWorld = serverWorld;
    }

    ServerBotNetwork(ServerBotNetwork original) {
        super(original);
        serverWorld = original.serverWorld;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof ServerBotNetwork serverNet) || !serverWorld.equals(serverNet.serverWorld))
            return false;

        return super.equals(obj);
    }

    Set<ServerBotNetwork> getSubnetworks() {
        Set<ServerBotNetwork> subnetworks = new HashSet<>();

        Queue<ChunkPos> toSubnet = new LinkedList<>(roboportMap.keySet());
        Set<ChunkPos> subneted = new HashSet<>();
        while (!toSubnet.isEmpty()) {
            ChunkPos subnetingChunk = toSubnet.remove();
            if (subneted.contains(subnetingChunk))
                continue;

            Set<BlockPos> subnetworkRoboports = new HashSet<>();
            Set<BlockPos> subnetworkLogisticStorages = new HashSet<>();

            Queue<ChunkPos> toVisit = new LinkedList<>(List.of(subnetingChunk));
            Set<ChunkPos> visited = new HashSet<>(Set.of(subnetingChunk));
            while (!toVisit.isEmpty()) {
                ChunkPos visitingChunk = toVisit.remove();
                if (!roboportMap.keySet().contains(visitingChunk))
                    continue;

                subnetworkRoboports.addAll(roboportMap.get(visitingChunk));
                if (logisticStorageMap.containsKey(visitingChunk)) {
                    subnetworkLogisticStorages.addAll(logisticStorageMap.get(visitingChunk));
                }

                ChunkPos.stream(visitingChunk, 1).forEach(pos -> {
                    if (visited.add(pos))
                        toVisit.add(pos);
                });
            }

            ServerBotNetwork serverNetwork = new ServerBotNetwork(subnetworkRoboports, subnetworkLogisticStorages, teamUuid, serverWorld);

            if (equals(serverNetwork))
                return Set.of(this);

            subnetworks.add(serverNetwork);
            subneted.addAll(subnetworkRoboports.stream().map(roboportPos -> new ChunkPos(roboportPos)).toList());
        }

        return subnetworks;
    }

    //#region Roboports
    boolean addRoboport(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> roboportsInChunk = roboportMap.computeIfAbsent(chunkPos, chunk -> new HashSet<>());

        return roboportsInChunk.add(pos);
    }

    record RemovedObjects(Optional<BlockPos> roboport, Optional<ChunkPos> chunk, Set<ServerBotNetwork> subnetworks) {
        public static final RemovedObjects NONE = new RemovedObjects(Optional.empty(), Optional.empty(), Set.of());
    }

    RemovedObjects removeRoboport(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);

        if (!roboportMap.containsKey(chunkPos))
            return RemovedObjects.NONE;

        Set<BlockPos> roboportsInChunk = roboportMap.get(chunkPos);

        if (!roboportsInChunk.contains(pos))
            return RemovedObjects.NONE;

        roboportsInChunk.remove(pos);
        if (!roboportsInChunk.isEmpty())
            return new RemovedObjects(Optional.of(pos), Optional.empty(), Set.of());

        roboportMap.remove(chunkPos);
        logisticStorageMap.remove(chunkPos);
        Set<ServerBotNetwork> subnetworks = getSubnetworks();

        if (subnetworks.size() <= 1)
            return new RemovedObjects(Optional.of(pos), Optional.of(chunkPos), Set.of());

        BotNetwork biggestSubnetwork = subnetworks.stream().max(Comparator.comparingInt(subnetwork -> subnetwork.roboportMap.keySet().size())).get();
        subnetworks.remove(biggestSubnetwork);
        for (BotNetwork subnetwork : subnetworks) {
            for (ChunkPos chunk : subnetwork.roboportMap.keySet()) {
                roboportMap.remove(chunk);
                logisticStorageMap.remove(chunk);
            }
        }

        return new RemovedObjects(Optional.of(pos), Optional.of(chunkPos), subnetworks);
    }
    //#endregion

    //#region Logistic Storages
    LogisticStorage<?> getLogisticStorage(BlockPos pos) {
        BlockEntity blockEntity = serverWorld.getBlockEntity(pos);

        if (blockEntity == null || !(blockEntity instanceof LogisticStorage<?> logisticStorage))
            throw new IllegalStateException(BotNetworkManager.class.getSimpleName() + " had an invalid logistic storage " + BlockPos.class.getSimpleName());

        return logisticStorage;
    }

    boolean addLogisticStorage(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> logisticStoragesInChunk = logisticStorageMap.computeIfAbsent(chunkPos, chunk -> new HashSet<>());

        return logisticStoragesInChunk.add(pos);
    }

    boolean removeLogisticStorage(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);

        if (!logisticStorageMap.containsKey(chunkPos))
            return false;

        Set<BlockPos> logisticStoragesInChunk = logisticStorageMap.get(chunkPos);

        if (!logisticStoragesInChunk.contains(pos))
            return false;

        logisticStoragesInChunk.remove(pos);
        if (logisticStoragesInChunk.isEmpty()) {
            logisticStorageMap.remove(chunkPos);
        }

        return true;
    }
    //#endregion

    //#region Reservations
    public boolean hasStacks(List<PredicateItemStack> predicateItemStacks) {
        if (predicateItemStacks.isEmpty())
            return true;

        for (BlockPos pos : getLogisticStorages()) {
            LogisticStorage<?> storage = getLogisticStorage(pos);
            List<Integer> storageReservations = storage.getReservationList();
            predicateItemStacks = InventoryUtils.getStacks(storage, storageReservations, predicateItemStacks, false).stream()
                    .filter(stack -> stack.count() != 0).toList();
            if (predicateItemStacks.isEmpty())
                return true;
        }

        return false;
    }

    Map<BlockPos, Collection<Reservation>> getReservations(UUID entityUuid) {
        if (logisticStorageMap.isEmpty())
            return Map.of();

        Map<BlockPos, Collection<Reservation>> blockReservations = new HashMap<>();

        for (BlockPos pos : getLogisticStorages()) {
            Collection<Reservation> reservations = getLogisticStorage(pos).getReservations(entityUuid);

            if (reservations.isEmpty())
                continue;

            blockReservations.computeIfAbsent(pos, ignored -> new ArrayList<>()).addAll(reservations);
        }

        return blockReservations;
    }

    void reserve(UUID entityUuid, List<PredicateItemStack> predicateItemStacks) {
        if (predicateItemStacks.isEmpty())
            return;

        List<PredicateItemStack> unreservedPredicateItemStacks = new ArrayList<>(predicateItemStacks);
        for (BlockPos pos : getLogisticStorages()) {

            LogisticStorage<?> storage = getLogisticStorage(pos);
            for (int i = 0; i < unreservedPredicateItemStacks.size(); i++) {
                while (unreservedPredicateItemStacks.get(i).count() > 0) {

                    List<Integer> reservationList = storage.getReservationList();
                    PredicateItemStack unreservedPredicateItemStack = unreservedPredicateItemStacks.get(i);
                    Optional<Integer> slot = InventoryUtils.getSlot(storage, reservationList, unreservedPredicateItemStack.predicate());

                    if (slot.isEmpty())
                        break;

                    int availableCount = storage.getStack(slot.get()).getCount() - reservationList.get(slot.get());
                    int reserveCount = Math.min(unreservedPredicateItemStack.count(), availableCount);
                    unreservedPredicateItemStacks.set(i, unreservedPredicateItemStack.copyWithCount(unreservedPredicateItemStack.count() - reserveCount));
                    storage.reserve(entityUuid, slot.get(), reserveCount);
                }
            }

            unreservedPredicateItemStacks.removeIf(predicateItemStack -> predicateItemStack.count() == 0);
            if (unreservedPredicateItemStacks.isEmpty())
                break;
        }
    }

    void unreserve(UUID entityUuid, BlockPos pos, int slot, int count) {
        getLogisticStorage(pos).unreserve(entityUuid, slot, count);
    }
    //#endregion

    Optional<BotEntity> getOrSpawnBotFor(BotJob job) {
        Set<BotType> capableBotTypes = AutomataBots.getBotTypesFor(job);
        if (capableBotTypes.isEmpty())
            return Optional.empty();

        for (ChunkPos chunkPos : roboportMap.keySet()) {
            BotEntity bot = null;

            Box searchBox = ChunkBounds.of(chunkPos, 0, serverWorld).toBox();
            List<BotEntity> teamBots = serverWorld.getEntitiesByType(TypeFilter.instanceOf(BotEntity.class), searchBox, //
                    botEntity -> botEntity.isAlive() && botEntity.isOnTeam(teamUuid));
            for (BotEntity botEntity : teamBots) {
                if (botEntity.canDoJob(job)) {
                    bot = botEntity;
                    break;
                }
            }

            if (bot == null)
                continue;

            reserve(bot.getUuid(), InventoryUtils.getStacks(bot, job.getPreferredStacks(serverWorld), false));
            return Optional.of(bot);
        }

        if (!hasStacks(job.getRequiredStacks(serverWorld)))
            return Optional.empty();

        Optional<RoboportBlockEntity> closestCapableRoboport = roboportMap.values().stream().flatMap(roboportsInChunk -> roboportsInChunk.stream()) //
                .map(roboportPos -> (RoboportBlockEntity)serverWorld.getBlockEntity(roboportPos)).filter(roboport -> roboport.canSpawnBotFor(job)) //
                .min(Comparator.comparingDouble(port -> port.getPos().getSquaredDistance(job.pos())));

        if (closestCapableRoboport.isEmpty())
            return Optional.empty();

        Optional<BotEntity> spawnedBot = closestCapableRoboport.get().spawnBotFor(job);
        reserve(spawnedBot.get().getUuid(), job.getPreferredStacks(serverWorld));

        return spawnedBot;
    }
}
