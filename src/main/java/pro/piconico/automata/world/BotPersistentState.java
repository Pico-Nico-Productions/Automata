package pro.piconico.automata.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import pro.piconico.automata.block.RoboportBlock;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.job.DeconstructionJob;
import pro.piconico.automata.bot.job.DeconstructionJobAssignment;
import pro.piconico.automata.entity.ConstructionBotEntity;
import pro.piconico.automata.registry.AutomataPersistentStates;

public class BotPersistentState extends PersistentState {
    public static final Codec<BotPersistentState> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BlockPos.CODEC.listOf().fieldOf("roboports").forGetter(state -> state.roboports.stream().toList()), DeconstructionJobAssignment.CODEC
                    .listOf().fieldOf("deconstruction_jobs").forGetter(state -> new ArrayList<>(state.deconstructionJobs.values())))
            .apply(instance, BotPersistentState::new));

    @FunctionalInterface
    public interface Mutate {
        void onMutate(ServerWorld handler);
    }

    public static final Event<Mutate> ROBOPORTS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (serverWorld) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(serverWorld);
        }
    });
    public static final Event<Mutate> DECONSTRUCTION_JOBS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (serverWorld) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(serverWorld);
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
    public Collection<BlockPos> getRoboports() {
        return Collections.unmodifiableSet(roboports);
    }

    public Optional<BlockPos> findClosestRoboport(BlockPos blockPos, Predicate<BlockPos> roboportPredicate) {
        Optional<BlockPos> closestRoboport = Optional.empty();
        double minDistance = Double.MAX_VALUE;
        for (BlockPos roboport : roboports) {
            double distance = roboport.getSquaredDistance(blockPos);
            if (distance >= minDistance || !roboportPredicate.test(blockPos))
                continue;

            closestRoboport = Optional.of(roboport);
            minDistance = distance;
        }

        return closestRoboport;
    }

    public Optional<BlockPos> findClosestRoboportInRange(BlockPos blockPos) {
        Predicate<BlockPos> inRange = roboport -> roboport.getChebyshevDistance(blockPos) < RoboportBlock.RANGE;
        return findClosestRoboport(blockPos, inRange);
    }

    private void addRoboport(BlockPos blockPos, ServerWorld serverWorld) {
        if (!roboports.add(blockPos))
            return;

        markDirty();
        ROBOPORTS_MUTATED.invoker().onMutate(serverWorld);
    }

    private void removeRoboport(BlockPos blockPos, ServerWorld serverWorld) {
        if (!roboports.remove(blockPos))
            return;

        markDirty();
        ROBOPORTS_MUTATED.invoker().onMutate(serverWorld);
    }
    //#endregion

    //#region Deconstruction Jobs
    public Collection<DeconstructionJob> getDeconstructionJobs() {
        return deconstructionJobs.values().stream().map(jobAssignment -> jobAssignment.job).toList();
    }

    public Collection<DeconstructionJob> getUnassignedDeconstructionJobs() {
        return deconstructionJobs.values().stream().filter(assignment -> !assignment.isAssigned()).map(jobAssignment -> jobAssignment.job).toList();
    }

    public void addDeconstructionJobs(Collection<DeconstructionJob> toAdd, ServerWorld serverWorld) {
        Stream<DeconstructionJob> addableJobs = toAdd.stream().filter(job -> !deconstructionJobs.containsKey(job.pos()) || !deconstructionJobs.get(job.pos()).isAssigned());
        if (addableJobs.filter(job -> deconstructionJobs.put(job.pos(), new DeconstructionJobAssignment(job)) == null).count() == 0)
            return;

        markDirty();
        DECONSTRUCTION_JOBS_MUTATED.invoker().onMutate(serverWorld);
    }

    public void removeDeconstructionJobs(Collection<DeconstructionJob> toRemove, ServerWorld serverWorld) {
        if (toRemove.stream().filter(job -> deconstructionJobs.remove(job.pos()) != null).count() == 0)
            return;

        markDirty();
        DECONSTRUCTION_JOBS_MUTATED.invoker().onMutate(serverWorld);
    }

    public void assignDeconstructionJobsToClosestRoboportInRange(Collection<DeconstructionJob> jobs, ServerWorld serverWorld) {
        boolean mutated = false;
        for (DeconstructionJob job : jobs) {
            Optional<BlockPos> roboportPos = findClosestRoboportInRange(job.pos());
            if (roboportPos.isEmpty())
                continue;

            BlockEntity blockEntity = serverWorld.getBlockEntity(roboportPos.get());
            if (!(blockEntity instanceof RoboportBlockEntity roboportEntity))
                throw new IllegalStateException();

            Optional<ConstructionBotEntity> constructionBot = roboportEntity.assignJob(job);
            if (constructionBot.isEmpty())
                continue;

            deconstructionJobs.get(job.pos()).setAssignedBot(Optional.of(constructionBot.get().getUuid()));
            constructionBot.get().JOB_ENDED.register(completed -> {
                if (completed) {
                    deconstructionJobs.remove(job.pos());
                }
                else {
                    deconstructionJobs.get(job.pos()).setAssignedBot(Optional.empty());
                }

                markDirty();
                DECONSTRUCTION_JOBS_MUTATED.invoker().onMutate(serverWorld);

            });
            mutated = true;
        }

        if (!mutated)
            return;

        markDirty();
        DECONSTRUCTION_JOBS_MUTATED.invoker().onMutate(serverWorld);
    }
    //#endregion

    public static void initialize() {
        RoboportBlock.PLACED.register((pos, world) -> {
            BotPersistentState botState = AutomataPersistentStates.get(world, AutomataPersistentStates.BOT_PERSISTENT_STATE);
            botState.addRoboport(pos, world);
        });
        RoboportBlock.REMOVED.register((pos, world) -> {
            BotPersistentState botState = AutomataPersistentStates.get(world, AutomataPersistentStates.BOT_PERSISTENT_STATE);
            botState.removeRoboport(pos, world);
        });
    }
}
