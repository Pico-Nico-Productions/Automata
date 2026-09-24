package pro.piconico.automata.block;

import org.jspecify.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import pro.piconico.automata.block.entity.LogisticChestBlockEntity;
import pro.piconico.automata.registry.AutomataEntities;

public class LogisticChestBlock extends BlockWithEntity {
    public static final MapCodec<LogisticChestBlock> CODEC = createCodec(LogisticChestBlock::new);
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final VoxelShape SHAPE = Block.createColumnShape(14.0, 0.0, 14.0);

    public LogisticChestBlock(Settings settings) {
        super(settings.pistonBehavior(PistonBehavior.BLOCK));
        setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LogisticChestBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient() || type != AutomataEntities.LOGISTIC_CHEST)
            return null;

        return (tickWorld, pos, tickState, blockEntity) -> ((LogisticChestBlockEntity)blockEntity).tickClient(tickWorld, pos, tickState);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient())
            return ActionResult.SUCCESS;

        LogisticChestBlockEntity logisticChest = world.getBlockEntity(pos, AutomataEntities.LOGISTIC_CHEST).get();
        player.openHandledScreen(logisticChest);

        return ActionResult.SUCCESS;
    }
}
