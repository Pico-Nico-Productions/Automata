package pro.piconico.automata.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.BlockUtils;
import pro.piconico.automata.block.entity.BlockEntityUtils;
import pro.piconico.automata.component.BlueprintComponent;
import pro.piconico.automata.component.BlueprintComponent.BlueprintBlockState;
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

        BlockPos min = BlockPosUtils.min(selections).get(), max = BlockPosUtils.max(selections).get();
        Optional<BlockPos> anchor = BlockPosUtils
                .min(BlockPos.stream(min, max).filter(pos -> BlockUtils.isConstructable(serverWorld, pos)).map(BlockPos::toImmutable).toList());

        if (anchor.isEmpty())
            return blueprintStack;

        List<BlueprintBlockState> blockStates = new ArrayList<>();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);

                    if (!BlockUtils.isConstructable(serverWorld, blockPos))
                        continue;

                    BlockState blockState = serverWorld.getBlockState(blockPos);
                    Optional<BlockEntity> blockEntity = Optional.ofNullable(serverWorld.getBlockEntity(blockPos));
                    Optional<NbtCompound> entityData = blockEntity.map(be -> BlockEntityUtils.createNbtWithId(be, serverWorld.getRegistryManager()));
                    blockStates.add(new BlueprintBlockState(blockPos.subtract(anchor.get()), blockState, entityData));
                }
            }
        }

        BlueprintComponent.set(blueprintStack, blockStates);

        return blueprintStack;
    }
}
