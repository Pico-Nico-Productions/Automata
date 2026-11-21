package pro.piconico.automata.bot.mode;

import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import pro.piconico.automata.component.BlueprintComponent;
import pro.piconico.automata.component.BotTeamComponent;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.component.BlueprintComponent.BlueprintBlockState;
import pro.piconico.automata.registry.AutomataAutomatoolModes;

public class ConstructionAutomatoolMode {
    public static boolean canDispatch(PlayerEntity player, ItemStack stack, boolean sendFailMessage) {
        if (!AutomatoolMode.canDispatch(AutomataAutomatoolModes.CONSTRUCTION, player, stack, sendFailMessage))
            return false;

        return !BlueprintComponent.get(stack).isEmpty();
    }

    public static void dispatch(UUID teamUuid, ServerWorld serverWorld, BlockPos anchor, Direction horizontalFacing,
            Map<BlockPos, BlueprintBlockState> blockStates) {
        // TODO: Complete dispatch
    }

    public static void dispatch(ServerPlayerEntity serverPlayer, ItemStack stack) {
        if (!AutomataAutomatoolModes.CONSTRUCTION.canDispatch(serverPlayer, stack, false))
            return;

        UUID teamUuid = BotTeamComponent.get(stack).get();
        SelectionComponent selection = SelectionComponent.get(stack).get();
        ServerWorld serverWorld = selection.getWorld(serverPlayer).get();
        BlockPos anchor = selection.getSelections().getFirst();
        Direction facing = serverPlayer.getHorizontalFacing();
        Map<BlockPos,BlueprintBlockState> blockStates = BlueprintComponent.get(stack);
        dispatch(teamUuid, serverWorld, anchor, facing, blockStates);
    }
}
