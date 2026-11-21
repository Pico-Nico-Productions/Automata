package pro.piconico.automata.bot.mode;

import java.util.List;
import java.util.Optional;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.item.BlueprintItem;
import pro.piconico.automata.registry.AutomataAutomatoolModes;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.util.math.BlockPosUtils;

public class BlueprintAutomatoolMode {
    public static void dispatch(Optional<ServerPlayerEntity> serverPlayer, ServerWorld serverWorld, List<BlockPos> selections) {
        if (selections.isEmpty()) {
            serverPlayer.ifPresent(player -> player.sendMessage(AutomataTexts.getSelectionInvalid(), true));
            return;
        }

        ItemStack blueprintStack = BlueprintItem.create(serverWorld, selections);
        if (serverPlayer.isPresent()) {
            serverPlayer.get().giveOrDropStack(blueprintStack);
        }
        else {
            BlockPos pos = BlockPosUtils.min(selections).get().up();
            ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), blueprintStack);
            serverWorld.spawnEntity(itemEntity);
        }
    }

    public static void dispatch(ServerPlayerEntity serverPlayer, ItemStack stack) {
        if (!AutomataAutomatoolModes.BLUEPRINT.canDispatch(serverPlayer, stack, true))
            return;

        SelectionComponent selection = SelectionComponent.get(stack).get();
        dispatch(Optional.of(serverPlayer), selection.getWorld(serverPlayer).get(), selection.getSelections());
    }
}
