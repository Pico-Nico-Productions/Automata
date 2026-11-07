package pro.piconico.automata.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.component.BlueprintComponent;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.util.math.BlockPosUtils;

public class BlueprintItem extends Item {
    public BlueprintItem(Settings settings) {
        super(settings);
    }

    public static ItemStack create(ServerWorld serverWorld, List<BlockPos> selections) {
        ItemStack blueprintStack = new ItemStack(AutomataItems.BLUEPRINT);

        if (selections.isEmpty())
            return blueprintStack;

        Map<BlockPos, BlockState> blocks = new HashMap<>();
        BlockPos min = BlockPosUtils.min(selections).get(), max = BlockPosUtils.max(selections).get();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    BlockState blockState = serverWorld.getBlockState(blockPos);

                    if (blockState.isAir())
                        continue;

                    blocks.put(blockPos.subtract(min), blockState);
                }
            }
        }
        BlueprintComponent.set(blueprintStack, blocks);

        return blueprintStack;
    }
}
