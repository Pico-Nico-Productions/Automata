package pro.piconico.automata.world;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import pro.piconico.automata.block.RoboportBlock;
import pro.piconico.automata.registry.AutomataPersistentStates;

public class BotPersistentState extends PersistentState {
    public static final Codec<BotPersistentState> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BlockPos.CODEC.listOf().fieldOf("roboports").forGetter(state -> state.roboports.stream().toList()),
                    BlockPos.CODEC.listOf().fieldOf("deconstruction_jobs").forGetter(state -> state.deconstructionJobs.stream().toList()))
            .apply(instance, BotPersistentState::new));

    @FunctionalInterface
    public interface Mutate {
        void onMutate(ServerWorld handler);
    }

    public static final Event<Mutate> ROBOPORTS_MUTATE = EventFactory.createArrayBacked(Mutate.class, callbacks -> (serverWorld) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(serverWorld);
        }
    });
    public static final Event<Mutate> DECONSTRUCTION_JOBS_MUTATE = EventFactory.createArrayBacked(Mutate.class, callbacks -> (serverWorld) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(serverWorld);
        }
    });

    private final HashSet<BlockPos> roboports = new HashSet<>();
    private final HashSet<BlockPos> deconstructionJobs = new HashSet<>();

    public BotPersistentState() {
    }

    public BotPersistentState(List<BlockPos> roboports, List<BlockPos> deconstructionJobs) {
        this.roboports.addAll(roboports);
        this.deconstructionJobs.addAll(deconstructionJobs);
    }

    //#region Roboports
    public Set<BlockPos> getRoboports() {
        return roboports;
    }

    public void addRoboport(BlockPos blockPos, ServerWorld serverWorld) {
        if (!roboports.add(blockPos))
            return;

        markDirty();
        ROBOPORTS_MUTATE.invoker().onMutate(serverWorld);
    }

    public void removeRoboport(BlockPos blockPos, ServerWorld serverWorld) {
        if (!roboports.remove(blockPos))
            return;

        markDirty();
        ROBOPORTS_MUTATE.invoker().onMutate(serverWorld);
    }
    //#endregion

    //#region Deconstruction
    public Set<BlockPos> getDeconstructionJobs() {
        return deconstructionJobs;
    }

    public void addDeconstructionJobs(Collection<BlockPos> toAdd, ServerWorld serverWorld) {
        if (!deconstructionJobs.addAll(toAdd))
            return;

        markDirty();
        DECONSTRUCTION_JOBS_MUTATE.invoker().onMutate(serverWorld);
    }

    public void removeDeconstructionJobs(Collection<BlockPos> toRemove, ServerWorld serverWorld) {
        if (!deconstructionJobs.removeAll(toRemove))
            return;

        markDirty();
        DECONSTRUCTION_JOBS_MUTATE.invoker().onMutate(serverWorld);
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
