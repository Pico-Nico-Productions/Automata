package pro.piconico.automata.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import pro.piconico.automata.Automata;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobAssignmentMap;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataPersistentStates;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

public class BotJobPersistentState extends PersistentState {
    public enum Mutation {
        ADD, REMOVE, MODIFY
    }

    @FunctionalInterface
    public interface Mutate {
        void onMutate(ServerWorld handler, Mutation mutation);
    }

    public static final Event<Mutate> JOBS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (serverWorld, mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(serverWorld, mutation);
        }
    });

    public static final Codec<BotJobPersistentState> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(BotJobAssignmentMap.CODEC.fieldOf("job_assignments").forGetter(state -> state.jobAssignmentMap)).apply(instance,
                    BotJobPersistentState::new));

    private final BotJobAssignmentMap jobAssignmentMap;

    public BotJobPersistentState() {
        jobAssignmentMap = new BotJobAssignmentMap();
    }

    private BotJobPersistentState(BotJobAssignmentMap jobAssignmentMap) {
        this.jobAssignmentMap = jobAssignmentMap;
    }

    private static BotJobPersistentState getJobState(ServerWorld serverWorld) {
        return serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_JOB_PERSISTENT_STATE);
    }

    //#region Job Querying
    public static Optional<BotJobAssignment> getJob(UUID teamUuid, BlockPos pos, BotJobType<?> jobType, ServerWorld serverWorld) {
        BotJobPersistentState jobState = getJobState(serverWorld);

        if (!jobState.jobAssignmentMap.containsKey(teamUuid))
            return Optional.empty();

        Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> blockMap = jobState.jobAssignmentMap.get(teamUuid);

        if (!blockMap.containsKey(pos))
            return Optional.empty();

        Map<BotJobType<?>, BotJobAssignment> typeMap = blockMap.get(pos);

        if (!typeMap.containsKey(jobType))
            return Optional.empty();

        return Optional.of(typeMap.get(jobType));
    }
    
    public static Set<BotJobAssignment> getJobs(ChunkBounds chunkBounds, ServerWorld serverWorld) {
        Set<BotJobAssignment> inRangeJobs = new HashSet<>();

        BotJobPersistentState jobState = getJobState(serverWorld);
        for (Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> blockMap : jobState.jobAssignmentMap.values()) {
            for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> entry : blockMap.entrySet()) {
                if (!chunkBounds.containsXZ(entry.getKey()))
                    continue;

                inRangeJobs.addAll(entry.getValue().values());
            }
        }

        return inRangeJobs;
    }
    //#endregion

    //#region Job Assignment
    private static boolean assignJob(BotJobAssignment jobAssignment, ServerWorld serverWorld, boolean notify) {
        Optional<BotEntity> botEntity = BotNetworkManager.assignJob(jobAssignment.JOB, serverWorld);

        if (botEntity.isEmpty())
            return false;

        jobAssignment.setAssignedBot(Optional.of(botEntity.get().getUuid()));

        if (notify) {
            getJobState(serverWorld).markDirty();
            JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.MODIFY);
        }

        return true;
    }

    private static void assignJobs(ServerWorld serverWorld) {
        BotJobPersistentState jobState = getJobState(serverWorld);
        boolean mutated = false;

        for (Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> blockMap : jobState.jobAssignmentMap.values()) {
            for (Map<BotJobType<?>, BotJobAssignment> typeMap : blockMap.values()) {
                for (BotJobAssignment jobAssignment : typeMap.values()) {
                    if (jobAssignment.isAssigned() || !assignJob(jobAssignment, serverWorld, false))
                        continue;

                    mutated = true;
                }
            }
        }

        if (!mutated)
            return;

        jobState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.MODIFY);
    }

    private static void unassignJob(BotJobAssignment jobAssignment, ServerWorld serverWorld, boolean notify) {
        UUID assignedBot = jobAssignment.getAssignedBot().get();
        jobAssignment.setAssignedBot(Optional.empty());

        if (notify) {
            getJobState(serverWorld).markDirty();
            JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.MODIFY);
        }

        Entity entity = serverWorld.getEntity(assignedBot);
        if (!(entity instanceof BotEntity botEntity)) {
            Automata.logError(BotJobAssignment.class.getSimpleName() + " assigned to an invalid " + UUID.class.getSimpleName(), IllegalStateException::new);
            return;
        }

        botEntity.endJob(false);
    }

    private static void unassignJobs(ServerWorld serverWorld) {
        BotJobPersistentState jobState = getJobState(serverWorld);
        boolean mutated = false;

        for (Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> blockMap : jobState.jobAssignmentMap.values()) {
            for (Map<BotJobType<?>, BotJobAssignment> typeMap : blockMap.values()) {
                for (BotJobAssignment jobAssignment : typeMap.values()) {
                    if (!jobAssignment.isAssigned() || BotNetworkManager.getNetwork(new ChunkPos(jobAssignment.JOB.pos()), serverWorld).isPresent())
                        continue;

                    unassignJob(jobAssignment, serverWorld, false);

                    mutated = true;
                }
            }
        }

        if (!mutated)
            return;

        jobState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.MODIFY);
    }
    //#endregion

    //#region Job Addition
    public static int addJobs(UUID teamUuid, Iterable<BotJob> jobs, ServerWorld serverWorld) {
        BotJobPersistentState jobState = getJobState(serverWorld);
        Set<BlockPos> addedPositions = new HashSet<>();

        Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> blockMap = jobState.jobAssignmentMap.computeIfAbsent(teamUuid, uuid -> new HashMap<>());
        for (BotJob job : jobs) {
            Map<BotJobType<?>, BotJobAssignment> typeMap = blockMap.computeIfAbsent(job.pos(), pos -> new HashMap<>());
            BotJobType<?> type = job.getType();

            if (typeMap.containsKey(type) && (typeMap.get(type).isAssigned() || typeMap.get(type).JOB.equals(job)))
                continue;

            typeMap.put(type, new BotJobAssignment(job, teamUuid));

            addedPositions.add(job.pos());
        }

        if (addedPositions.isEmpty())
            return 0;

        jobState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.ADD);

        assignJobs(serverWorld);

        return addedPositions.size();
    }
    //#endregion

    //#region Job Removal
    private static boolean removeJob(UUID teamUuid, BlockPos pos, BotJobType<?> type, ServerWorld serverWorld, boolean notify) {
        BotJobPersistentState jobState = getJobState(serverWorld);

        if (getJob(teamUuid, pos, type, serverWorld).isEmpty())
            return false;

        Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> blockMap = jobState.jobAssignmentMap.get(teamUuid);
        Map<BotJobType<?>, BotJobAssignment> typeMap = blockMap.get(pos);
        BotJobAssignment jobAssignment = typeMap.remove(type);
        if (typeMap.isEmpty()) {
            blockMap.remove(pos);
            if (blockMap.isEmpty()) {
                jobState.jobAssignmentMap.remove(teamUuid);
            }
        }
        unassignJob(jobAssignment, serverWorld, false);

        if (notify) {
            jobState.markDirty();
            JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.REMOVE);
        }

        return true;
    }

    public static int removeJobs(ServerWorld serverWorld) {
        BotJobPersistentState jobState = getJobState(serverWorld);

        if (jobState.jobAssignmentMap.isEmpty())
            return 0;

        int removeCount = jobState.jobAssignmentMap.size();
        jobState.jobAssignmentMap.clear();

        jobState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.REMOVE);

        return removeCount;
    }
    //#endregion

    private static void onNetworksMutated(ServerWorld serverWorld, BotNetworkManager.Mutation mutation) {
        switch (mutation) {
        case BotNetworkManager.Mutation.ADD:
            assignJobs(serverWorld);
            break;
        case BotNetworkManager.Mutation.REMOVE:
            unassignJobs(serverWorld);
            break;
        }
    }

    private static void onBotAdded(RoboportBlockEntity roboport) {
        if (!(roboport.getWorld() instanceof ServerWorld serverWorld))
            return;

        assignJobs(serverWorld);
    }

    private static void onJobEnded(BotEntity botEntity, BotJob job, boolean completed) {
        if (!(botEntity.getEntityWorld() instanceof ServerWorld serverWorld))
            return;

        Optional<BotJobAssignment> jobAssignment = getJob(botEntity.getTeamUuid(), job.pos(), job.getType(), serverWorld);
        if (jobAssignment.isEmpty())
            return;

        if (completed) {
            removeJob(botEntity.getTeamUuid(), job.pos(), job.getType(), serverWorld, true);
        }
        else {
            if (!assignJob(jobAssignment.get(), serverWorld, true)) {
                unassignJob(jobAssignment.get(), serverWorld, true);
            }
        }
    }

    public static void initialize() {
        BotNetworkManager.NETWORKS_MUTATED.register(BotJobPersistentState::onNetworksMutated);
        RoboportBlockEntity.BOT_ADDED.register(BotJobPersistentState::onBotAdded);
        BotEntity.JOB_ENDED.register(BotJobPersistentState::onJobEnded);
    }
}
