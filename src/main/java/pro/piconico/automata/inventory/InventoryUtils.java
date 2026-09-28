package pro.piconico.automata.inventory;

import java.util.function.Predicate;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryUtils {
    public static int canAdd(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty())
            return 0;

        int canAddCount = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);

            if (slotStack.isEmpty()) {
                canAddCount += inventory.getMaxCount(stack);
            }
            else if (ItemStack.areItemsAndComponentsEqual(stack, slotStack)) {
                canAddCount += inventory.getMaxCount(stack) - slotStack.getCount();
            }

            if (canAddCount >= stack.getCount())
                return stack.getCount();
        }

        return canAddCount;
    }

    public static boolean canAdd(Inventory inventory, Item item) {
        return canAdd(inventory, new ItemStack(item)) == 1;
    }

    private static void simulateAdd(Inventory inventory, ItemStack[] copies, ItemStack stack, Predicate<ItemStack> targetPredicate) {
        for (int i = 0; i < copies.length; i++) {
            ItemStack slotStack = copies[i];

            if (!targetPredicate.test(slotStack))
                continue;

            int addCount = Math.min(stack.getCount(), inventory.getMaxCount(stack) - slotStack.getCount());
            if (addCount == 0)
                continue;

            copies[i] = new ItemStack(stack.getItem(), slotStack.getCount() + addCount);
            stack.decrement(addCount);

            if (stack.getCount() == 0)
                return;
        }
    }

    public static int canAdd(Inventory inventory, Inventory toAdd) {
        int canAddCount = 0;

        ItemStack[] inventoryCopy = new ItemStack[inventory.size()];

        for (int i = 0; i < inventoryCopy.length; i++) {
            inventoryCopy[i] = inventory.getStack(i).copy();
        }

        for (int i = 0; i < toAdd.size(); i++) {
            ItemStack stackToAdd = toAdd.getStack(i).copy();
            int toAddCount = stackToAdd.getCount();

            simulateAdd(inventory, inventoryCopy, stackToAdd, slotStack -> ItemStack.areItemsAndComponentsEqual(stackToAdd, slotStack));
            if (stackToAdd.isEmpty()) {
                canAddCount += toAddCount;
                continue;
            }

            simulateAdd(inventory, inventoryCopy, stackToAdd, slotStack -> slotStack.isEmpty());
            canAddCount += toAddCount - stackToAdd.getCount();
        }

        return canAddCount;
    }

    private static void add(Inventory inventory, ItemStack stack, Predicate<ItemStack> targetPredicate) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);

            if (!targetPredicate.test(slotStack))
                continue;

            int addCount = Math.min(stack.getCount(), inventory.getMaxCount(stack) - slotStack.getCount());
            if (addCount == 0)
                continue;

            inventory.setStack(i, new ItemStack(stack.getItem(), slotStack.getCount() + addCount));
            stack.decrement(addCount);

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

    public static int add(Inventory inventory, Inventory toAdd) {
        int added = 0;

        for (ItemStack stack : toAdd) {
            added += add(inventory, stack);
        }

        return added;
    }
}
