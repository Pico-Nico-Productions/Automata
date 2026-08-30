package pro.piconico.automata.mixin;

import java.util.HashMap;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import pro.piconico.automata.event.HoldItemCallback;

@Mixin(PlayerEntity.class)
public abstract class PlayerHoldingTrackerMixin {
    @Unique
    private final Map<Hand, ItemStack> LAST_HELD_ITEMS = new HashMap<>(Hand.values().length);

    @Inject(method = "tick", at = @At("TAIL"))
    private void trackHandChanges(CallbackInfo callbackInfo) {
        PlayerEntity player = (PlayerEntity)(Object)this;

        for (Hand hand : Hand.values()) {
            ItemStack lastStack = LAST_HELD_ITEMS.getOrDefault(hand, ItemStack.EMPTY);
            ItemStack currentStack = player.getStackInHand(hand);

            if (ItemStack.areItemsAndComponentsEqual(lastStack, currentStack))
                continue;

            if (!lastStack.isEmpty()) {
                HoldItemCallback.HOLD_ENDED.invoker().onHold(player, hand, lastStack);
            }
            if (!currentStack.isEmpty()) {
                HoldItemCallback.HOLD_STARTED.invoker().onHold(player, hand, currentStack);
            }

            LAST_HELD_ITEMS.put(hand, currentStack.copy());
        }
    }
}
