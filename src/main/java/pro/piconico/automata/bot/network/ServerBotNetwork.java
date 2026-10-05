package pro.piconico.automata.bot.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.device.LogisticStorage;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.item.ItemUtils.PredicateItemStack;
import pro.piconico.automata.registry.AutomataBots;
import pro.piconico.automata.util.MapUtils;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

public class ServerBotNetwork extends BotNetwork {
    final ServerWorld serverWorld;
    private final Map<BlockPos, Map<Integer, Integer>> reservations;
    private final Map<UUID, Map<BlockPos, Map<Integer, Integer>>> reservationLookup;

    ServerBotNetwork(Collection<BlockPos> roboports, Collection<BlockPos> logisticStorages, UUID teamUuid, ServerWorld serverWorld) {
        super(roboports, logisticStorages, teamUuid);
        this.serverWorld = serverWorld;
        reservations = new HashMap<>();
        reservationLookup = new HashMap<>();
    }

    ServerBotNetwork(ServerBotNetwork original) {
        super(original);
        serverWorld = original.serverWorld;
        reservations = original.reservations.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> Map.copyOf(entry.getValue())));
        reservationLookup = original.reservationLookup.entrySet().stream().collect(Collectors.toMap(Entry::getKey,
                entry -> entry.getValue().entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> Map.copyOf(e.getValue())))));
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
        reservations.remove(pos);
        Iterator<Entry<UUID, Map<BlockPos, Map<Integer, Integer>>>> reservationIterator = reservationLookup.entrySet().iterator();
        while (reservationIterator.hasNext()) {
            Entry<UUID, Map<BlockPos, Map<Integer, Integer>>> entry = reservationIterator.next();

            if (!entry.getValue().containsKey(pos))
                continue;

            if (entry.getValue().size() == 1) {
                reservationIterator.remove();
                continue;
            }

            entry.getValue().remove(pos);
        }

        return true;
    }
    //#endregion

    //#region Reservations
    private List<Integer> flattenReservations(LogisticStorage<?> storage) {
        BlockPos pos = storage.getPos();

        if (!reservations.containsKey(pos))
            return new ArrayList<>(Collections.nCopies(storage.size(), 0));

        Map<Integer, Integer> storageReservations = reservations.get(pos);
        List<Integer> flattenedReservations = IntStream.range(0, storage.size()).map(i -> storageReservations.getOrDefault(i, 0)).boxed().toList();

        return flattenedReservations;
    }

    public boolean hasStacks(List<PredicateItemStack> predicateItemStacks) {
        if (predicateItemStacks.isEmpty())
            return true;

        for (BlockPos pos : getLogisticStorages()) {
            LogisticStorage<?> storage = getLogisticStorage(pos);
            List<Integer> storageReservations = flattenReservations(storage);
            predicateItemStacks = InventoryUtils.getStacks(storage, storageReservations, predicateItemStacks, false).stream()
                    .filter(stack -> stack.count() != 0).toList();
            if (predicateItemStacks.isEmpty())
                return true;
        }

        return false;
    }

    Map<BlockPos, Map<Integer, Integer>> getReservations(UUID entityUuid) {
        if (!reservationLookup.containsKey(entityUuid))
            return Map.of();

        return Collections.unmodifiableMap(reservationLookup.get(entityUuid));
    }

    private void reserve(UUID entityUuid, List<PredicateItemStack> predicateItemStacks) {
        if (predicateItemStacks.isEmpty())
            return;

        List<PredicateItemStack> unreservedPredicateItemStacks = new ArrayList<>(predicateItemStacks);
        for (BlockPos pos : getLogisticStorages()) {

            LogisticStorage<?> storage = getLogisticStorage(pos);
            for (int i = 0; i < unreservedPredicateItemStacks.size(); i++) {
                while (unreservedPredicateItemStacks.get(i).count() > 0) {

                    List<Integer> flatReservations = flattenReservations(storage);
                    PredicateItemStack unreservedPredicateItemStack = unreservedPredicateItemStacks.get(i);
                    Optional<Integer> slot = InventoryUtils.getSlot(storage, flatReservations, unreservedPredicateItemStack.predicate());

                    if (slot.isEmpty())
                        break;

                    int availableCount = storage.getStack(slot.get()).getCount() - flatReservations.get(slot.get());
                    int reserveCount = Math.min(unreservedPredicateItemStack.count(), availableCount);
                    unreservedPredicateItemStacks.set(i, unreservedPredicateItemStack.copyWithCount(unreservedPredicateItemStack.count() - reserveCount));
                    BiFunction<Object, Integer, Integer> incrementReservationCount = (ignored, count) -> (count != null ? count : 0) + reserveCount;
                    MapUtils.computeNested(reservations, incrementReservationCount, pos, slot.get());
                    MapUtils.computeNested(reservationLookup, incrementReservationCount, entityUuid, pos, slot.get());
                }
            }
            unreservedPredicateItemStacks.removeIf(predicateItemStack -> predicateItemStack.count() == 0);
            if (unreservedPredicateItemStacks.isEmpty())
                break;
        }
    }

    private void unreserve(Map<BlockPos, Map<Integer, Integer>> reservations, BlockPos pos, int slot, int count) {
        MapUtils.<Integer>getNested(reservations, pos, slot).ifPresent(reservedCount -> {
            if (count >= reservedCount) {
                MapUtils.removeNested(reservations, pos, slot);
            }
            else {
                MapUtils.computeNested(reservations, (ignored, ignored2) -> reservedCount - count, pos, slot);
            }
        });
    }

    void unreserve(UUID entityUuid, BlockPos pos, int slot, int count) {
        unreserve(reservations, pos, slot, count);
        Map<BlockPos, Map<Integer, Integer>> blockReservations = reservationLookup.get(entityUuid);
        unreserve(blockReservations, pos, slot, count);
        if (blockReservations.isEmpty()) {
            reservationLookup.remove(entityUuid);
        }
    }
    //#endregion

    Optional<BotEntity> getOrSpawnBotFor(BotJob job) {
        Set<BotType> capableBotTypes = AutomataBots.getBotTypesFor(job);
        if (capableBotTypes.isEmpty())
            return Optional.empty();

        for (ChunkPos chunkPos : roboportMap.keySet()) {
            Box searchBox = ChunkBounds.of(chunkPos, 0, serverWorld).toBox();
            List<BotEntity> capableBots = serverWorld.getEntitiesByType(TypeFilter.instanceOf(BotEntity.class), searchBox, //
                    botEntity -> botEntity.isAlive() && botEntity.isOnTeam(teamUuid) && botEntity.canDoJob(job));

            if (capableBots.isEmpty())
                continue;

            return Optional.of(capableBots.getFirst());
        }

        if (!hasStacks(job.getRequiredStacks(serverWorld)))
            return Optional.empty();

        Optional<RoboportBlockEntity> closestCapableRoboport = roboportMap.values().stream().flatMap(roboportsInChunk -> roboportsInChunk.stream()) //
                .map(roboportPos -> (RoboportBlockEntity)serverWorld.getBlockEntity(roboportPos)).filter(roboport -> roboport.canSpawnBotFor(job)) //
                .min(Comparator.comparingDouble(port -> port.getPos().getSquaredDistance(job.pos())));

        if (closestCapableRoboport.isEmpty())
            return Optional.empty();

        Optional<BotEntity> bot = closestCapableRoboport.get().spawnBotFor(job);
        reserve(bot.get().getUuid(), job.getPreferredStacks(serverWorld));

        return bot;
    }
}
