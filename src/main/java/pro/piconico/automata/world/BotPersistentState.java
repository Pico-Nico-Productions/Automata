package pro.piconico.automata.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import pro.piconico.automata.bot.job.DeconstructionJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataPersistentStates;

public class BotPersistentState extends PersistentState {
    public static final Codec<BotPersistentState> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(BlockPos.CODEC.listOf().fieldOf("roboports").forGetter(botState -> botState.roboports.stream().toList()),
                    BotJobAssignment.createCodec(DeconstructionJob.CODEC).listOf().fieldOf("deconstruction_jobs")
                            .forGetter(botState -> new ArrayList<>(botState.deconstructionJobs.values())))
                    .apply(instance, BotPersistentState::new));

    public enum Mutation {
        Add, Remove, Modify
    }

    @FunctionalInterface
    public interface Mutate {
        void onMutate(ServerWorld handler, Mutation mutation);
    }

    private record BotJobType<JobType extends BotJob>(HashMap<BlockPos, BotJobAssignment<JobType>> jobs, Event<Mutate> mutateEvent) {
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
    private final HashMap<BlockPos, BotJobAssignment<DeconstructionJob>> deconstructionJobs = new HashMap<>();
    private final HashMap<Class<? extends BotJob>, BotJobType<? extends BotJob>> botJobTypeRegistry = new HashMap<>();

    public BotPersistentState() {
        botJobTypeRegistry.put(DeconstructionJob.class, new BotJobType<DeconstructionJob>(deconstructionJobs, DECONSTRUCTION_JOBS_MUTATED));
    }

    public BotPersistentState(List<BlockPos> roboports, List<BotJobAssignment<DeconstructionJob>> deconstructionJobs) {
        this();

        this.roboports.addAll(roboports);
        for (BotJobAssignment<DeconstructionJob> jobAssignment : deconstructionJobs) {
            this.deconstructionJobs.put(jobAssignment.job.pos(), jobAssignment);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends BotJob> BotJobType<T> getBotJobType(Class<T> jobClass) {
        if (!botJobTypeRegistry.containsKey(jobClass)) {
            Automata.logError("Tried to get the bot job type of an unregister type", IllegalArgumentException::new);
        }

        return (BotJobType<T>)botJobTypeRegistry.get(jobClass);
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
    private static <T extends BotJob> void assignJobs(Class<T> jobClass, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        BotJobType<T> botJobType = botState.getBotJobType(jobClass);
        boolean mutated = false;

        for (BotJobAssignment<T> jobAssignment : botJobType.jobs.values()) {
            if (jobAssignment.isAssigned())
                continue;

            BotJob job = jobAssignment.getJob();

            Optional<RoboportBlockEntity> roboportEntity = BotPersistentState.getRoboportClosestTo(job.pos(), roboport -> roboport.canDoJob(job), serverWorld);
            if (roboportEntity.isEmpty())
                continue;

            BotEntity botEntity = roboportEntity.get().assignJob(job).get();
            jobAssignment.setAssignedBot(Optional.of(botEntity.getUuid()));

            mutated = true;
        }

        if (!mutated)
            return;

        botState.markDirty();
        botState.getBotJobType(jobClass).mutateEvent.invoker().onMutate(serverWorld, Mutation.Modify);
    }

    public static <T extends BotJob> int addJobs(Class<T> jobClass, Iterable<T> toAdd, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        BotJobType<T> botJobType = botState.getBotJobType(jobClass);
        int addCount = 0;

        for (T job : toAdd) {
            if (botJobType.jobs.containsKey(job.pos()) && botJobType.jobs.get(job.pos()).isAssigned())
                continue;

            botJobType.jobs.put(job.pos(), new BotJobAssignment<T>(job));

            addCount++;
        }
        if (addCount == 0)
            return 0;

        botState.markDirty();
        botJobType.mutateEvent.invoker().onMutate(serverWorld, Mutation.Add);

        assignJobs(jobClass, serverWorld);

        return addCount;
    }

    public static <T extends BotJob> int removeJobs(Class<T> jobClass, Iterable<T> toRemove, ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        BotJobType<T> botJobType = botState.getBotJobType(jobClass);
        int removeCount = 0;

        for (T job : toRemove) {
            if (!botJobType.jobs.containsKey(job.pos()))
                continue;

            BotJobAssignment<T> jobAssignment = botJobType.jobs.remove(job.pos());

            removeCount++;

            Optional<UUID> assignedBotUUID = jobAssignment.getAssignedBot();
            if (assignedBotUUID.isEmpty())
                continue;

            Entity entity = serverWorld.getEntity(assignedBotUUID.get());
            if (!(entity instanceof BotEntity assignedBot)) {
                Automata.logError(BotPersistentState.class.getSimpleName() + " has a job assigned to an invalid " + UUID.class.getSimpleName() + ".",
                        IllegalStateException::new);
                continue;
            }

            assignedBot.endJob(false);
        }
        if (removeCount == 0)
            return 0;

        botState.markDirty();
        botJobType.mutateEvent.invoker().onMutate(serverWorld, Mutation.Remove);

        return removeCount;
    }

    public static int clearJobs(ServerWorld serverWorld) {
        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);

        return removeJobs(DeconstructionJob.class, botState.deconstructionJobs.values().stream().map(jobAssignment -> jobAssignment.job).toList(), serverWorld);
    }

    private static void onJobEnded(BotEntity botEntity, BotJob job, boolean completed) {
        if (!(botEntity.getEntityWorld() instanceof ServerWorld serverWorld))
            return;

        BotPersistentState botState = serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_PERSISTENT_STATE);
        BotJobType<?> botJobType = botState.getBotJobType(job.getClass());

        if (!botJobType.jobs.containsKey(job.pos())) {
            return;
        }

        if (completed) {
            botJobType.jobs.remove(job.pos());
        }
        else {
            botJobType.jobs.get(job.pos()).setAssignedBot(Optional.empty());
        }

        botState.markDirty();
        botJobType.mutateEvent.invoker().onMutate(serverWorld, completed ? Mutation.Remove : Mutation.Modify);

        if (!completed)
            assignJobs(job.getClass(), serverWorld);
    }
    //#endregion

    public static void initialize() {
        RoboportBlock.PLACED.register(BotPersistentState::addRoboport);
        RoboportBlock.REMOVED.register(BotPersistentState::removeRoboport);

        BotEntity.JOB_ENDED.register(BotPersistentState::onJobEnded);
    }
}
