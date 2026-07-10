package pro.piconico.automata.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.registry.AutomataBlockEntities;

public class RoboportBlockEntity extends BlockEntity {
    public RoboportBlockEntity(BlockPos pos, BlockState state) {
        super(AutomataBlockEntities.ROBOPORT, pos, state);
    }
}