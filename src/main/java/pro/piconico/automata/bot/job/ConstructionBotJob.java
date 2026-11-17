package pro.piconico.automata.bot.job;

import java.util.List;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.item.ItemUtils.PredicateItemStack;
import pro.piconico.automata.registry.AutomataBotJobs;

public record ConstructionBotJob(BlockPos pos, BlockState blockState) implements BotJob {
    public static final MapCodec<ConstructionBotJob> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            BlockPos.CODEC.fieldOf("pos").forGetter(ConstructionBotJob::pos), //
            BlockState.CODEC.fieldOf("block").forGetter(ConstructionBotJob::blockState) //
    ).apply(instance, ConstructionBotJob::new));

    @Override
    public BotJobType<?> getType() {
        return AutomataBotJobs.CONSTRUCTION;
    }

    @Override
    public List<PredicateItemStack> getRequiredStacks(World world) {
        return List.of(new PredicateItemStack(stack -> stack.isOf(blockState.getBlock().asItem())));
    }

    @Override
    public boolean canStart(World world) {
        return world.canPlace(blockState, pos, ShapeContext.absent());
    }

    @Override
    public TickResult tick(ServerWorld serverWorld, BotEntity bot, int tick) {
        if (!canStart(serverWorld))
            return TickResult.FAILED;

        serverWorld.setBlockState(pos, blockState);

        return TickResult.SUCCEEDED;
    }
}
