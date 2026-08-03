package pro.piconico.automata.inventory;

import java.util.function.Predicate;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryUtils {
    public static boolean canAdd(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty())
            return true;

        int remainingCount = stack.getCount();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);

            if (slotStack.isEmpty()) {
                remainingCount -= inventory.getMaxCount(stack);
            }
            else if (ItemStack.areItemsAndComponentsEqual(stack, slotStack)) {
                remainingCount -= inventory.getMaxCount(slotStack) - slotStack.getCount();
            }

            if (remainingCount <= 0)
                return true;
        }

        return false;
    }

    public static boolean canAdd(Inventory inventory, Item item) {
        return canAdd(inventory, new ItemStack(item));
    }

    private static void add(Inventory inventory, ItemStack stack, Predicate<ItemStack> targePredicate) {
        Item item = stack.getItem();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);

            if (!targePredicate.test(slotStack))
                continue;

            int addedCount = Math.min(stack.getCount(), slotStack.getMaxCount() - slotStack.getCount());
            if (addedCount == 0)
                continue;

            stack.decrement(addedCount);
            inventory.setStack(i, new ItemStack(item, slotStack.getCount() + addedCount));

            if (stack.getCount() == 0)
                return;
        }
    }

    public static int add(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int toAddCount = stack.getCount();
        add(inventory, stack, slotStack -> ItemStack.areItemsAndComponentsEqual(stack, slotStack));
        if (stack.isEmpty())
            return toAddCount;

        add(inventory, stack, slotStack -> slotStack.isEmpty());

        return toAddCount - stack.getCount();
    }

    public static boolean add(Inventory inventory, Item item) {
        return add(inventory, new ItemStack(item)) > 0;
    }
}
