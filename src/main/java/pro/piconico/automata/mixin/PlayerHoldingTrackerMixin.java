package pro.piconico.automata.mixin;

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
    private ItemStack lastMainHand = ItemStack.EMPTY;
    @Unique
    private ItemStack lastOffHand = ItemStack.EMPTY;

    @Inject(method = "tick", at = @At("TAIL"))
    private void trackHandChanges(CallbackInfo callbackInfo) {
        PlayerEntity player = (PlayerEntity)(Object)this;

        ItemStack currentMainHand = player.getMainHandStack();
        checkHandSlot(player, Hand.MAIN_HAND, lastMainHand, currentMainHand);
        this.lastMainHand = currentMainHand.copy();

        ItemStack currentOffHand = player.getOffHandStack();
        checkHandSlot(player, Hand.OFF_HAND, lastOffHand, currentOffHand);
        this.lastOffHand = currentOffHand.copy();
    }

    @Unique
    private void checkHandSlot(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        if (ItemStack.areItemsAndComponentsEqual(oldStack, newStack))
            return;

        if (!oldStack.isEmpty()) {
            HoldItemCallback.HOLD_ENDED.invoker().onHold(player, hand, oldStack);
        }
        if (!newStack.isEmpty()) {
            HoldItemCallback.HOLD_STARTED.invoker().onHold(player, hand, newStack);
        }
    }
}
