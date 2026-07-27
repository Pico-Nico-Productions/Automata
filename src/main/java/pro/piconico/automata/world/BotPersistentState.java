package pro.piconico.automata.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
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
import pro.piconico.automata.bot.job.DeconstructionJob;
import pro.piconico.automata.bot.job.DeconstructionJobAssignment;
import pro.piconico.automata.entity.ConstructionBotEntity;
import pro.piconico.automata.registry.AutomataPersistentStates;

public class BotPersistentState extends PersistentState {
    public static final Codec<BotPersistentState> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BlockPos.CODEC.listOf().fieldOf("roboports").forGetter(botState -> botState.roboports.stream().toList()), DeconstructionJobAssignment.CODEC
                    .listOf().fieldOf("deconstruction_jobs").forGetter(botState -> new ArrayList<>(botState.deconstructionJobs.values())))
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
    public static final Event<Mutate> DECONSTRUCTION_JOBS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (serverWorld, mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(serverWorld, mutation);
        }
    });

    private final HashSet<BlockPos> roboports = new HashSet<>();
    private final HashMap<BlockPos, DeconstructionJobAssignment> deconstructionJobs = new HashMap<>();

    public BotPersistentState(List<BlockPos> roboports, List<DeconstructionJobAssignment> deconstructionJobs) {
        this.roboports.addAll(roboports);
        for (DeconstructionJobAssignment deconstructionJobAssignment : deconstructionJobs) {
            this.deconstructionJobs.put(deconstructionJobAssignment.job.pos(), deconstructionJobAssignment);
        }
    }

    public BotPersistentState() {
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
    private static <AssignmentType extends BotJobAssignment> void assignJobs(HashMap<BlockPos, AssignmentType> jobs,
            Event<Mutate> mutateEvent, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        boolean mutated = false;

        for (AssignmentType jobAssignment : jobs.values()) {
            if (jobAssignment.isAssigned())
                continue;

            BotJob job = jobAssignment.getJob();

            Optional<RoboportBlockEntity> roboportEntity = BotPersistentState.getRoboportClosestTo(job.pos(), roboport -> roboport.canDoJob(job), serverWorld);
            if (roboportEntity.isEmpty())
                continue;

            ConstructionBotEntity constructionBot = roboportEntity.get().assignJob(job).get();
            jobAssignment.setAssignedBot(Optional.of(constructionBot.getUuid()));

            mutated = true;
        }

        if (!mutated)
            return;

        botState.markDirty();
        mutateEvent.invoker().onMutate(serverWorld, Mutation.Modify);
    }

    private static <JobType extends BotJob, AssignmentType extends BotJobAssignment> int addJobs(Collection<JobType> toAdd,
            HashMap<BlockPos, AssignmentType> jobs, Function<JobType, AssignmentType> assignmentFactory,
            Event<Mutate> mutateEvent, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        int addCount = 0;

        for (JobType job : toAdd) {
            if (jobs.containsKey(job.pos()) && jobs.get(job.pos()).isAssigned())
                continue;

            jobs.put(job.pos(), assignmentFactory.apply(job));

            addCount++;
        }
        if (addCount == 0)
            return 0;

        botState.markDirty();
        mutateEvent.invoker().onMutate(serverWorld, Mutation.Add);

        assignJobs(jobs, mutateEvent, serverWorld);

        return addCount;
    }

    private static <JobType extends BotJob, AssignmentType extends BotJobAssignment> int removeJobs(Collection<BlockPos> toRemove,
            HashMap<BlockPos, AssignmentType> jobs, Event<Mutate> mutateEvent, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        int removeCount = 0;

        for (BlockPos jobPos : toRemove) {
            if (!jobs.containsKey(jobPos))
                continue;

            AssignmentType jobAssignment = jobs.remove(jobPos);

            removeCount++;

            Optional<UUID> assignedBotUUID = jobAssignment.getAssignedBot();
            if (assignedBotUUID.isEmpty())
                continue;

            Entity entity = serverWorld.getEntity(assignedBotUUID.get());
            if (!(entity instanceof ConstructionBotEntity assignedBot)) {
                Automata.logError(BotPersistentState.class.getSimpleName() + " has a job assigned to an invalid " + UUID.class.getSimpleName() + ".", IllegalStateException::new);
                continue;
            }

            assignedBot.setJob(Optional.empty());
        }
        if (removeCount == 0)
            return 0;

        botState.markDirty();
        mutateEvent.invoker().onMutate(serverWorld, Mutation.Remove);

        return removeCount;
    }

    public static int clearJobs(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        return removeJobs(botState.deconstructionJobs.keySet(), botState.deconstructionJobs, DECONSTRUCTION_JOBS_MUTATED, serverWorld);
    }

    private static void onJobEnded(ConstructionBotEntity bot, BotJob job, boolean completed) {
        if (!(bot.getEntityWorld() instanceof ServerWorld serverWorld))
            return;

        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        HashMap<BlockPos, DeconstructionJobAssignment> jobs = botState.deconstructionJobs;
        if (!jobs.containsKey(job.pos())) {
            Automata.logError("Bot ended a nonexistent job.", IllegalStateException::new);
            return;
        }

        if (completed) {
            jobs.remove(job.pos());
        }
        else {
            jobs.get(job.pos()).setAssignedBot(Optional.empty());
        }

        botState.markDirty();
        DECONSTRUCTION_JOBS_MUTATED.invoker().onMutate(serverWorld, completed ? Mutation.Remove : Mutation.Modify);

        if (!completed) assignJobs(jobs, DECONSTRUCTION_JOBS_MUTATED, serverWorld);
    }
    //#endregion

    //#region Deconstruction Jobs
    public static Collection<DeconstructionJobAssignment> getDeconstructionJobs(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        return botState.deconstructionJobs.values();
    }

    public static Collection<DeconstructionJobAssignment> getAssignedDeconstructionJobs(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        return botState.deconstructionJobs.values().stream().filter(assignment -> assignment.isAssigned()).toList();
    }

    public static Collection<DeconstructionJobAssignment> getUnassignedDeconstructionJobs(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        return botState.deconstructionJobs.values().stream().filter(assignment -> !assignment.isAssigned()).toList();
    }

    public static int addDeconstructionJobs(Collection<DeconstructionJob> toAdd, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        return addJobs(toAdd, botState.deconstructionJobs, DeconstructionJobAssignment::new, DECONSTRUCTION_JOBS_MUTATED, serverWorld);
    }

    public static int removeDeconstructionJobs(Collection<BlockPos> toRemove, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        return removeJobs(toRemove, botState.deconstructionJobs, DECONSTRUCTION_JOBS_MUTATED, serverWorld);
    }

    public static int clearDeconstructionJobs(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        return removeDeconstructionJobs(botState.deconstructionJobs.keySet(), serverWorld);
    }
    //#endregion

    public static void initialize() {
        RoboportBlock.PLACED.register(BotPersistentState::addRoboport);
        RoboportBlock.REMOVED.register(BotPersistentState::removeRoboport);

        ConstructionBotEntity.JOB_ENDED.register(BotPersistentState::onJobEnded);
    }
}
