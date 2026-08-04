package pro.piconico.automata.client.event.handler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import pro.piconico.automata.client.network.BotCache;
import pro.piconico.automata.event.HoldItemCallback;
import pro.piconico.automata.item.CommandToolItem;

public class CommandToolEventHandler {
    private static void onHoldEnded(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!(stack.getItem() instanceof CommandToolItem))
            return;

        if (player.getMainHandStack().getItem() instanceof CommandToolItem || player.getOffHandStack().getItem() instanceof CommandToolItem)
            return;

        if (!player.getEntityWorld().isClient())
            return;

        BotCache.jobAssignments.clear();
    }

    public static void initialize() {
        HoldItemCallback.HOLD_ENDED.register(CommandToolEventHandler::onHoldEnded);
    }
}
