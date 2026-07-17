package pro.piconico.automata.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.block.entity.RoboportBlockEntity;

public class RoboportBlock extends BlockWithEntity {
    public static final MapCodec<RoboportBlock> CODEC = createCodec(RoboportBlock::new);

    public RoboportBlock(Settings settings) {
        super(settings.strength(3.0f));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RoboportBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof RoboportBlockEntity roboport) {
            player.openHandledScreen(roboport);
        }

        return ActionResult.SUCCESS;
    }
}