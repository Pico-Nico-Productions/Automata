package pro.piconico.automata.bot.job;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

public record DeconstructionJob(BlockPos pos) implements BotJob {
    public static final Codec<DeconstructionJob> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(job -> job.pos)).apply(instance, DeconstructionJob::new));
}
