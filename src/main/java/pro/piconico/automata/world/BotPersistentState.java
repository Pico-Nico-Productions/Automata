package pro.piconico.automata.world;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.PersistentState;
import pro.piconico.automata.Automata;
import pro.piconico.automata.block.RoboportBlock;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataPersistentStates;

public class BotPersistentState extends PersistentState {
    public static final Codec<BotPersistentState> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BlockPos.CODEC.listOf().fieldOf("roboports").forGetter(state -> state.roboports.stream().toList()),
                    BotJobAssignment.createCodec(BotJob.CODEC).listOf().fieldOf("job_assignments")
                            .forGetter(state -> state.jobAssignments.values().stream().flatMap(innerMap -> innerMap.values().stream()).toList()))
            .apply(instance, BotPersistentState::new));

    public enum Mutation {
        Add, Remove, Modify
    }

    @FunctionalInterface
    public interface Mutate {
        void onMutate(ServerWorld handler, Mutation mutation);
    }

    public static final Event<Mutate> ROBOPORTS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (serverWorld, mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(serverWorld, mutation);
        }
    });
    public static final Event<Mutate> JOBS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (serverWorld, mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(serverWorld, mutation);
        }
    });

    private final HashSet<BlockPos> roboports = new HashSet<>(); // TODO: Consider converting to PointOfInterestType
    private final Map<BlockPos, Map<BotJobType<?>, BotJobAssignment<BotJob>>> jobAssignments = new HashMap<>();

    public BotPersistentState() {
    }

    public BotPersistentState(List<BlockPos> roboports, List<BotJobAssignment<BotJob>> jobAssignments) {
        this.roboports.addAll(roboports);

        for (BotJobAssignment<BotJob> assignment : jobAssignments) {
            BotJob job = assignment.getJob();
            BlockPos pos = job.pos();
            BotJobType<?> type = job.getType();
            this.jobAssignments.computeIfAbsent(pos, k -> new HashMap<>()).put(type, assignment);
        }
    }

    //#region Roboports
    public static Collection<BlockPos> getRoboports(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        return Collections.unmodifiableSet(botState.roboports);
    }

    public static Optional<RoboportBlockEntity> getRoboportClosestTo(Vec3i position, Predicate<RoboportBlockEntity> roboportPredicate,
            ServerWorld serverWorld) {
        Optional<RoboportBlockEntity> closestRoboport = Optional.empty();

        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        double minDistance = Double.MAX_VALUE;
        for (BlockPos roboportPos : botState.roboports) {
            double distance = roboportPos.getSquaredDistance(position);
            if (distance >= minDistance)
                continue;

            if (!(serverWorld.getBlockEntity(roboportPos) instanceof RoboportBlockEntity roboport)) {
                Automata.logError(BotPersistentState.class.getSimpleName() + " expected a roboport at " + roboportPos.toShortString() + ".",
                        IllegalStateException::new);
                continue;
            }

            if (!roboportPredicate.test(roboport))
                continue;

            closestRoboport = Optional.of(roboport);
            minDistance = distance;
        }

        return closestRoboport;
    }

    private static void addRoboport(BlockPos blockPos, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        if (!botState.roboports.add(blockPos))
            return;

        botState.markDirty();
        ROBOPORTS_MUTATED.invoker().onMutate(serverWorld, Mutation.Add);
    }

    private static void removeRoboport(BlockPos blockPos, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        if (!botState.roboports.remove(blockPos))
            return;

        botState.markDirty();
        ROBOPORTS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);
    }
    //#endregion

    //#region Jobs
    public static Map<BlockPos, Map<BotJobType<?>, BotJobAssignment<BotJob>>> getJobs(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        return Collections.unmodifiableMap(botState.jobAssignments);
    }

    public static Optional<BotJobAssignment<BotJob>> getJobAt(BlockPos pos, BotJobType<?> jobType, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        if (!botState.jobAssignments.containsKey(pos) || !botState.jobAssignments.get(pos).containsKey(jobType))
            return Optional.empty();

        return Optional.of(botState.jobAssignments.get(pos).get(jobType));
    }

    public static Optional<Collection<BotJobAssignment<BotJob>>> getJobsAt(BlockPos pos, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        if (!botState.jobAssignments.containsKey(pos))
            return Optional.empty();

        return Optional.of(botState.jobAssignments.get(pos).values());
    }

    private static void assignJobs(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        boolean mutated = false;

        for (Map<BotJobType<?>, BotJobAssignment<BotJob>> typeMap : botState.jobAssignments.values()) {
            for (BotJobAssignment<BotJob> jobAssignment : typeMap.values()) {
                if (jobAssignment.isAssigned())
                    continue;

                BotJob job = jobAssignment.getJob();

                Optional<RoboportBlockEntity> roboportEntity = BotPersistentState.getRoboportClosestTo(job.pos(), roboport -> roboport.canDoJob(job),
                        serverWorld);
                if (roboportEntity.isEmpty())
                    continue;

                BotEntity botEntity = roboportEntity.get().assignJob(job).get();
                jobAssignment.setAssignedBot(Optional.of(botEntity.getUuid()));

                mutated = true;
            }
        }

        if (!mutated)
            return;

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.Modify);
    }

    public static int addJobs(Iterable<BotJob> toAdd, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        Set<BlockPos> addedPositions = new HashSet<>();

        for (BotJob job : toAdd) {
            Map<BotJobType<?>, BotJobAssignment<BotJob>> typeMap = botState.jobAssignments.computeIfAbsent(job.pos(), pos -> new HashMap<>());
            BotJobType<?> type = job.getType();

            if (typeMap.containsKey(type) && (typeMap.get(type).isAssigned() || typeMap.get(type).job.equals(job)))
                continue;

            typeMap.put(type, new BotJobAssignment<>(job));

            addedPositions.add(job.pos());
        }

        if (addedPositions.isEmpty())
            return 0;

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.Add);

        assignJobs(serverWorld);

        return addedPositions.size();
    }

    private static void unassignJob(BotJobAssignment<BotJob> jobAssignment, ServerWorld serverWorld) {
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

    public static boolean removeJobAt(BlockPos pos, BotJobType<?> type, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        if (getJobAt(pos, type, serverWorld).isEmpty())
            return false;

        Map<BotJobType<?>, BotJobAssignment<BotJob>> typeMap = botState.jobAssignments.get(pos);
        BotJobAssignment<BotJob> jobAssignment = typeMap.remove(type);
        if (typeMap.isEmpty()) {
            botState.jobAssignments.remove(pos);
        }
        unassignJob(jobAssignment, serverWorld);

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);

        return true;
    }

    public static int removeJobsAt(BlockPos pos, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        if (!botState.jobAssignments.containsKey(pos))
            return 0;

        Map<BotJobType<?>, BotJobAssignment<BotJob>> typeMap = botState.jobAssignments.remove(pos);
        for (BotJobAssignment<BotJob> jobAssignment : typeMap.values()) {
            unassignJob(jobAssignment, serverWorld);
        }

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, Mutation.Remove);

        return typeMap.size();
    }

    public static int clearJobs(ServerWorld serverWorld, Set<BotJobType<?>> jobTypes) {
        int removeCount = 0;

        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        for (BlockPos jobPos : botState.jobAssignments.keySet()) {
            for (BotJobType<?> botJobType : jobTypes) {
                if (!removeJobAt(jobPos, botJobType, serverWorld))
                    continue;

                removeCount++;
            }
        }

        return removeCount;
    }

    private static void onJobEnded(BotEntity botEntity, BotJob job, boolean completed) {
        if (!(botEntity.getEntityWorld() instanceof ServerWorld serverWorld))
            return;

        Optional<BotJobAssignment<BotJob>> jobAssignment = getJobAt(job.pos(), job.getType(), serverWorld);
        if (jobAssignment.isEmpty())
            return;

        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        if (completed) {
            Map<BotJobType<?>, BotJobAssignment<BotJob>> typeMap = botState.jobAssignments.get(job.pos());
            typeMap.remove(job.getType());
            if (typeMap.isEmpty()) {
                botState.jobAssignments.remove(job.pos());
            }
        }
        else {
            jobAssignment.get().setAssignedBot(Optional.empty());
        }

        botState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, completed ? Mutation.Remove : Mutation.Modify);

        if (!completed)
            assignJobs(serverWorld);
    }
    //#endregion

    private static void onBotAdmitted(RoboportBlockEntity roboport) {
        if (!(roboport.getWorld() instanceof ServerWorld serverWorld))
            return;

        assignJobs(serverWorld);
    }

    public static void initialize() {
        RoboportBlock.PLACED.register(BotPersistentState::addRoboport);
        RoboportBlock.REMOVED.register(BotPersistentState::removeRoboport);

        BotEntity.JOB_ENDED.register(BotPersistentState::onJobEnded);
        RoboportBlockEntity.BOT_ADDED.register(BotPersistentState::onBotAdmitted);
    }
}
