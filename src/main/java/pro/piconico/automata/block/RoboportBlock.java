package pro.piconico.automata.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.registry.AutomataEntities;

public class RoboportBlock extends BotDeviceBlock<RoboportBlockEntity> {
    public static final MapCodec<RoboportBlock> CODEC = createCodec(RoboportBlock::new);

    public RoboportBlock(Settings settings) {
        super(settings.strength(3.0f));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntityType<RoboportBlockEntity> getEntityType() {
        return AutomataEntities.ROBOPORT;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RoboportBlockEntity(pos, state);
    }
}