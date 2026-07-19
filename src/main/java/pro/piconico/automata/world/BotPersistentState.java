package pro.piconico.automata.world;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class BotPersistentState extends PersistentState {
    public static final Codec<BotPersistentState> CODEC = RecordCodecBuilder.create(instance -> 
        instance.group(
            BlockPos.CODEC.listOf().fieldOf("deconstruction_jobs").forGetter(state -> state.deconstructionJobs.stream().toList())
        ).apply(instance, BotPersistentState::new)
    );

    private final HashSet<BlockPos> deconstructionJobs = new HashSet<>();
   
    public BotPersistentState() {
    }  

    public BotPersistentState(List<BlockPos> deconstructionJobs) {
        this.deconstructionJobs.addAll(deconstructionJobs);
    }

    public Set<BlockPos> getDeconstructionJobs() {
        return deconstructionJobs;
    }

    public void addDeconstructionJob(BlockPos blockPos) {
        if (!deconstructionJobs.add(blockPos))
            return;

        markDirty();
    }

    public void removeDeconstructionJob(BlockPos blockPos) {
        if (!deconstructionJobs.remove(blockPos))
            return;

        markDirty();
    }
}
