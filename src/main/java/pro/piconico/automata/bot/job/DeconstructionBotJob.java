package pro.piconico.automata.bot.job;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.BlockUtils;
import pro.piconico.automata.registry.AutomataBotJobs;

public record DeconstructionBotJob(BlockPos pos) implements BotJob {
    public static final MapCodec<DeconstructionBotJob> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(job -> job.pos)).apply(instance, DeconstructionBotJob::new));

    @Override
    public BotJobType<?> getType() {
        return AutomataBotJobs.DECONSTRUCTION;
    }

    @Override
    public boolean execute(ServerWorld serverWorld) {
        if (!BlockUtils.hasDeconstructableBlock(serverWorld, pos)) {
            return true;
        }

        if (BlockUtils.hasBreakableBlock(serverWorld, pos)) {
            serverWorld.breakBlock(pos, true, null);
        }
        if (BlockUtils.hasFluidSourceBlock(serverWorld, pos)) {
            serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        }

        return true;
    }
}
