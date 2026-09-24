package pro.piconico.automata.screen;

import org.spongepowered.asm.mixin.injection.invoke.arg.ArgumentIndexOutOfBoundsException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import pro.piconico.automata.util.function.QuadFunction;

public class ScreenHandlerUtils {
    public static final int SLOT_SIZE = 16, SLOT_SPACING = 2, BAR_SPACING = 6;
    public static final int SLOT_DELTA = SLOT_SIZE + SLOT_SPACING, BAR_DELTA = SLOT_SIZE + BAR_SPACING;

    public static int getRowCount(int inventorySize, int rowSize) {
        return inventorySize > 0 ? (inventorySize - 1) / rowSize + 1 : 0;
    }

    public static int getRowCount(int inventorySize) {
        return getRowCount(inventorySize, 9);
    }

    public static ItemStack quickMove(DefaultedList<Slot> slots, QuadFunction<ItemStack, Integer, Integer, Boolean, Boolean> insert, PlayerEntity player,
            int slotIndex, int handlerInventorySize) {
        if (slotIndex < 0 || slotIndex >= slots.size())
            throw new ArgumentIndexOutOfBoundsException(slotIndex);
        if (handlerInventorySize <= 0 || handlerInventorySize >= slots.size())
            throw new ArgumentIndexOutOfBoundsException(handlerInventorySize);

        ItemStack newStack = ItemStack.EMPTY;

        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (slotIndex < handlerInventorySize) {
                if (!insert.apply(originalStack, handlerInventorySize, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else {
                if (!insert.apply(originalStack, 0, handlerInventorySize, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            }
            else {
                slot.markDirty();
            }
        }

        return newStack;
    }
}
