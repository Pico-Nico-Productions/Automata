package pro.piconico.automata.bot.network;

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
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.device.LogisticStorage;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.registry.AutomataEntities;

// TODO: Add item reservation so bots can't both aim for the same item
public class ServerBotNetwork extends BotNetwork {
    final ServerWorld serverWorld;

    ServerBotNetwork(Collection<BlockPos> roboports, Collection<BlockPos> logisticStorages, UUID teamUuid, ServerWorld serverWorld) {
        super(roboports, logisticStorages, teamUuid);
        this.serverWorld = serverWorld;
    }

    ServerBotNetwork(ServerBotNetwork original) {
        super(original);
        this.serverWorld = original.serverWorld;
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

    private static LogisticStorage<?> getLogisticStorage(ServerWorld serverWorld, BlockPos pos) {
        BlockEntity blockEntity = serverWorld.getBlockEntity(pos);

        if (blockEntity == null || !(blockEntity instanceof LogisticStorage<?> logisticStorage))
            throw new IllegalStateException(BotNetworkManager.class.getSimpleName() + " had an invalid logistic storage " + BlockPos.class.getSimpleName());

        return logisticStorage;
    }

    boolean containsItem(Predicate<ItemStack> predicate) {
        for (BlockPos pos : getLogisticStorages()) {
            if (getLogisticStorage(serverWorld, pos).containsAny(predicate))
                return true;
        }

        return false;
    }

    // TODO: Refine blacklist to include stack item count
    boolean containsItems(Collection<Predicate<ItemStack>> predicates) {
        Map<BlockPos, LogisticStorage<?>> logisticStorages = new HashMap<>();
        Map<BlockPos, Set<Integer>> blacklists = new HashMap<>();
        for (Predicate<ItemStack> predicate : predicates) {
            boolean passed = false;

            for (BlockPos pos : getLogisticStorages()) {
                LogisticStorage<?> logisticStorage = logisticStorages.computeIfAbsent(pos, ignored -> getLogisticStorage(serverWorld, pos));
                Set<Integer> blacklist = blacklists.computeIfAbsent(pos, ignored -> new HashSet<>());
                Optional<Integer> slot = InventoryUtils.getSlot(logisticStorage, predicate, blacklist);

                if (slot.isEmpty())
                    continue;

                blacklist.add(slot.get());
                passed = true;
                break;
            }

            if (!passed)
                return false;
        }

        return true;
    }

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
        if (!logisticStoragesInChunk.isEmpty())
            return true;

        logisticStorageMap.remove(chunkPos);

        return true;
    }

    Optional<BotEntity> getOrSpawnBotFor(BotJob job) {
        Stream<BlockPos> capableRoboports = roboportMap.values().stream().flatMap(roboportsInChunk -> roboportsInChunk.stream()) //
                .filter(roboportPos -> serverWorld.getBlockEntity(roboportPos, AutomataEntities.ROBOPORT).map(roboport -> roboport.canDoJob(job))
                        .orElse(false));
        Optional<BlockPos> closestCapableRoboport = capableRoboports.min(Comparator.comparingDouble(pos -> pos.getSquaredDistance(job.pos())));

        if (closestCapableRoboport.isEmpty())
            return Optional.empty();

        return serverWorld.getBlockEntity(closestCapableRoboport.get(), AutomataEntities.ROBOPORT).get().getOrSpawnBotFor(job);
    }
}
