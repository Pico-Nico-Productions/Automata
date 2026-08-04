package pro.piconico.automata.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface HoldItemCallback {
    void onHold(PlayerEntity player, Hand hand, ItemStack stack);

    Event<HoldItemCallback> HOLD_STARTED = EventFactory.createArrayBacked(HoldItemCallback.class, (listeners) -> (player, hand, stack) -> {
        for (HoldItemCallback event : listeners) {
            event.onHold(player, hand, stack);
        }
    });

    Event<HoldItemCallback> HOLD_ENDED = EventFactory.createArrayBacked(HoldItemCallback.class, (listeners) -> (player, hand, stack) -> {
        for (HoldItemCallback event : listeners) {
            event.onHold(player, hand, stack);
        }
    });
}
