package pro.piconico.automata.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockWithEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.block.entity.RoboportBlockEntity;

public class RoboportBlock extends BlockWithEntity {
    public static final MapCodec<RoboportBlock> CODEC = createCodec(RoboportBlock::new);
    public static final int RANGE = 2;

    @FunctionalInterface
    public interface BlockAction {
        void onAction(BlockPos pos, ServerWorld world);
    }

    public static final Event<BlockAction> PLACED = EventFactory.createArrayBacked(BlockAction.class, callbacks -> (pos, world) -> {
        for (BlockAction callback : callbacks) {
            callback.onAction(pos, world);
        }
    });

    public static final Event<BlockAction> REMOVED = EventFactory.createArrayBacked(BlockAction.class, callbacks -> (pos, world) -> {
        for (BlockAction callback : callbacks) {
            callback.onAction(pos, world);
        }
    });

    public RoboportBlock(Settings settings) {
        super(settings.strength(3.0f).pistonBehavior(PistonBehavior.BLOCK));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (world.isClient() || state.isOf(oldState.getBlock()))
            return;

        PLACED.invoker().onAction(pos, (ServerWorld)world);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (world.isClient() || state.isOf(world.getBlockState(pos).getBlock()))
            return;

        REMOVED.invoker().onAction(pos, (ServerWorld)world);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RoboportBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient())
            return ActionResult.SUCCESS;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof RoboportBlockEntity roboport) {
            player.openHandledScreen(roboport);
        }

        return ActionResult.SUCCESS;
    }
}