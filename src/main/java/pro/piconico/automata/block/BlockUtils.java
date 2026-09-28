package pro.piconico.automata.block;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockUtils {
    public static boolean isBreakableBlock(World world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.isAir() || blockState.getHardness(world, blockPos) < 0f)
            return false;

        return blockState.contains(Properties.WATERLOGGED) || blockState.getFluidState().isEmpty();
    }

    public static boolean isFluidSourceBlock(World world, BlockPos blockPos) {
        return world.getBlockState(blockPos).getFluidState().isStill();
    }

    public static boolean isDeconstructable(World world, BlockPos blockPos) {
        return isBreakableBlock(world, blockPos) || isFluidSourceBlock(world, blockPos);
    }
}
