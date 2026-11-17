package pro.piconico.automata.block;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockUtils {
    public static final int BREAK_NOT_IN_PROGRESS = -1, MAX_BREAK_PROGRESS = 10;
    public static final float TOOL_SPEED = 1F / 30F, NO_TOOL_SPEED = 1F / 100F;

    public static boolean isPlaceableBlock(World world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.isAir())
            return false;

        return blockState.contains(Properties.WATERLOGGED) || blockState.getFluidState().isEmpty();
    }

    public static boolean isBreakableBlock(World world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getHardness(world, blockPos) < 0f)
            return false;

        return isPlaceableBlock(world, blockPos);
    }

    public static boolean isFluidSource(World world, BlockPos blockPos) {
        return world.getBlockState(blockPos).getFluidState().isStill();
    }

    public static boolean isConstructable(World world, BlockPos blockPos) {
        return isPlaceableBlock(world, blockPos) || isFluidSource(world, blockPos);
    }

    public static boolean isDeconstructable(World world, BlockPos blockPos) {
        return isBreakableBlock(world, blockPos) || isFluidSource(world, blockPos);
    }

    public static int getBreakProgress(float progress) {
        if (progress < 0F || progress >= 1F)
            return BREAK_NOT_IN_PROGRESS;

        return (int)(progress * BlockUtils.MAX_BREAK_PROGRESS);
    }
}
