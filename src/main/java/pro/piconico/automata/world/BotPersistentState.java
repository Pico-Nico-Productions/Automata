package pro.piconico.automata.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import net.minecraft.world.PersistentState;
import pro.piconico.automata.Automata;
import pro.piconico.automata.block.RoboportBlock;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.job.BotJobUtils;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataPersistentStates;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

public class BotPersistentState extends PersistentState {
    public static final Codec<BotPersistentState> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(BotJobUtils.JOB_ASSIGNMENT_CODEC.fieldOf("job_assignments").forGetter(state -> state.jobAssignmentMap))
                    .apply(instance, BotPersistentState::new));

    public enum Mutation {
        Add, Remove, Modify
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

    private final Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> jobAssignmentMap;

    public BotPersistentState() {
        jobAssignmentMap = new HashMap<>();
    }

    public BotPersistentState(Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> jobAssignmentMap) {
        this.jobAssignmentMap = jobAssignmentMap;
    }

    //#region Job Querying
    public static Optional<BotJobAssignment> getJobAt(BlockPos pos, BotJobType<?> jobType, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        if (!botState.jobAssignmentMap.containsKey(pos) || !botState.jobAssignmentMap.get(pos).containsKey(jobType))
            return Optional.empty();

        return Optional.of(botState.jobAssignmentMap.get(pos).get(jobType));
    }

    public static Collection<BotJobAssignment> getJobsAt(BlockPos pos, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        if (!botState.jobAssignmentMap.containsKey(pos))
            return List.of();

        return botState.jobAssignmentMap.get(pos).values();
    }

    public static Collection<BotJobAssignment> getJobsIn(ChunkBounds chunkBounds, ServerWorld serverWorld) {
        Collection<BotJobAssignment> inRangeJobs = new ArrayList<>();

        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        for (Map.Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> entry : botState.jobAssignmentMap.entrySet()) {
            if (!chunkBounds.containsXZ(entry.getKey()))
                continue;

            inRangeJobs.addAll(entry.getValue().values());
        }

        return inRangeJobs;
    }

    public static Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> getJobs(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        return Collections.unmodifiableMap(botState.jobAssignmentMap);
    }
    //#endregion

    //#region Job Assignment
    private static void assignJobs(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        boolean mutated = false;

        for (Map<BotJobType<?>, BotJobAssignment> typeMap : botState.jobAssignmentMap.values()) {
            for (BotJobAssignment jobAssignment : typeMap.values()) {
                if (jobAssignment.isAssigned())
                    continue;

                Optional<BotEntity> botEntity = BotNetworkManager.assignJob(jobAssignment.job, serverWorld);

                if (botEntity.isEmpty())
                    continue;

                jobAssignment.setAssignedBot(Optional.of(botEntity.get().getUuid()));

                mutated = true;
            }
        }

        if (!mutated)
            return;

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.Modify);
    }

    private static void unassignJob(BotJobAssignment jobAssignment, ServerWorld serverWorld) {
        if (jobAssignment.getAssignedBot().isEmpty())
            return;

        UUID assignedBot = jobAssignment.getAssignedBot().get();
        jobAssignment.setAssignedBot(Optional.empty());

        Entity entity = serverWorld.getEntity(assignedBot);
        if (!(entity instanceof BotEntity botEntity)) {
            Automata.logError(BotJobAssignment.class.getSimpleName() + " assigned to an invalid " + UUID.class.getSimpleName(), IllegalStateException::new);
            return;
        }

        botEntity.endJob(false);
    }
    //#endregion

    public static int addJobs(Iterable<BotJob> toAdd, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        Set<BlockPos> addedPositions = new HashSet<>();

        for (BotJob job : toAdd) {
            Map<BotJobType<?>, BotJobAssignment> typeMap = botState.jobAssignmentMap.computeIfAbsent(job.pos(), pos -> new HashMap<>());
            BotJobType<?> type = job.getType();

            if (typeMap.containsKey(type) && (typeMap.get(type).isAssigned() || typeMap.get(type).job.equals(job)))
                continue;

            typeMap.put(type, new BotJobAssignment(job));

            addedPositions.add(job.pos());
        }

        if (addedPositions.isEmpty())
            return 0;

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.Add);

        assignJobs(serverWorld);

        return addedPositions.size();
    }

    //#region Job Removal
    public static boolean removeJobAt(BlockPos pos, BotJobType<?> type, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        if (getJobAt(pos, type, serverWorld).isEmpty())
            return false;

        Map<BotJobType<?>, BotJobAssignment> typeMap = botState.jobAssignmentMap.get(pos);
        BotJobAssignment jobAssignment = typeMap.remove(type);
        if (typeMap.isEmpty()) {
            botState.jobAssignmentMap.remove(pos);
        }
        unassignJob(jobAssignment, serverWorld);

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);

        return true;
    }

    public static int removeJobsAt(BlockPos pos, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        if (!botState.jobAssignmentMap.containsKey(pos))
            return 0;

        Map<BotJobType<?>, BotJobAssignment> typeMap = botState.jobAssignmentMap.remove(pos);
        for (BotJobAssignment jobAssignment : typeMap.values()) {
            unassignJob(jobAssignment, serverWorld);
        }

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);

        return typeMap.size();
    }

    public static int removeJobsOf(Set<BotJobType<?>> jobTypes, ServerWorld serverWorld) {
        int removeCount = 0;

        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        Iterator<Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>>> jobEntryIterator = botState.jobAssignmentMap.entrySet().iterator();
        while (jobEntryIterator.hasNext()) {
            Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> jobEntry = jobEntryIterator.next();
            for (BotJobType<?> botJobType : jobTypes) {
                if (!jobEntry.getValue().containsKey(botJobType))
                    continue;

                BotJobAssignment jobAssignment = jobEntry.getValue().remove(botJobType);
                if (jobEntry.getValue().isEmpty()) {
                    jobEntryIterator.remove();
                }

                unassignJob(jobAssignment, serverWorld);

                removeCount++;
            }
        }

        if (removeCount == 0)
            return 0;

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);

        return removeCount;
    }
    //#endregion

    private static void onRoboportPlaced(BlockPos pos, ServerWorld serverWorld) {
        assignJobs(serverWorld);
    }

    private static void onNetworksMutated(ServerWorld serverWorld, BotNetworkManager.Mutation mutation) {
        if (mutation == BotNetworkManager.Mutation.Remove)
            return;

        assignJobs(serverWorld);
    }

    private static void onBotAdded(RoboportBlockEntity roboport) {
        if (!(roboport.getWorld() instanceof ServerWorld serverWorld))
            return;

        assignJobs(serverWorld);
    }

    private static void onJobEnded(BotEntity botEntity, BotJob job, boolean completed) {
        if (!(botEntity.getEntityWorld() instanceof ServerWorld serverWorld))
            return;

        Optional<BotJobAssignment> jobAssignment = getJobAt(job.pos(), job.getType(), serverWorld);
        if (jobAssignment.isEmpty())
            return;

        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        if (completed) {
            Map<BotJobType<?>, BotJobAssignment> typeMap = botState.jobAssignmentMap.get(job.pos());
            typeMap.remove(job.getType());
            if (typeMap.isEmpty()) {
                botState.jobAssignmentMap.remove(job.pos());
            }
        }
        else {
            jobAssignment.get().setAssignedBot(Optional.empty());
        }

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, completed ? Mutation.Remove : Mutation.Modify);

        assignJobs(serverWorld);
    }

    public static void initialize() {
        RoboportBlock.PLACED.register(BotPersistentState::onRoboportPlaced);
        BotNetworkManager.NETWORKS_MUTATED.register(BotPersistentState::onNetworksMutated);
        RoboportBlockEntity.BOT_ADDED.register(BotPersistentState::onBotAdded);
        BotEntity.JOB_ENDED.register(BotPersistentState::onJobEnded);
    }
}
