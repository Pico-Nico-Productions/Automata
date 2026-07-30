package pro.piconico.automata.bot.job;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.registry.AutomataBotJobs;

public record DeconstructionJob(BlockPos pos) implements BotJob {
    public static final MapCodec<DeconstructionJob> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(job -> job.pos)).apply(instance, DeconstructionJob::new));

    @Override
    public BotJobType<?> getType() {
        return AutomataBotJobs.DECONSTRUCTION_JOB;
    }
}
