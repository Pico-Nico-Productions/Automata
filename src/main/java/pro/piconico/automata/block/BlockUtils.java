package pro.piconico.automata.block;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockUtils {
    public static boolean hasBreakableBlock(World world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.isAir() || blockState.getHardness(world, blockPos) < 0f)
            return false;

        return blockState.contains(Properties.WATERLOGGED) || blockState.getFluidState().isEmpty();
    }

    public static boolean hasFluidSourceBlock(World world, BlockPos blockPos) {
        return world.getBlockState(blockPos).getFluidState().isStill();
    }
    
    public static boolean hasDeconstructableBlock(World world, BlockPos blockPos) {
        return hasBreakableBlock(world, blockPos) || hasFluidSourceBlock(world, blockPos);
    }
}
