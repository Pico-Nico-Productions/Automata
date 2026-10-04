package pro.piconico.automata.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pro.piconico.automata.item.ItemUtils.PredicateItemStack;

public class InventoryUtils {
    public enum SetStackResult {
        NONE, REPLACE, ADD, REMOVE
    }

    //#region Searching
    public static Optional<Integer> getSlot(Inventory inventory, Predicate<ItemStack> predicate) {
        for (int i = 0; i < inventory.size(); i++) {
            if (predicate.test(inventory.getStack(i)))
                return Optional.of(i);
        }

        return Optional.empty();
    }

    public static boolean hasStacks(Inventory inventory, List<PredicateItemStack> predicateItemStacks) {
        Map<Integer, Integer> reservations = new HashMap<>();
        for (PredicateItemStack predicateItemStack : predicateItemStacks) {
            int requiredCount = predicateItemStack.count();
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                int simulatedCount = stack.getCount() - reservations.getOrDefault(i, 0);

                if (simulatedCount == 0 || !predicateItemStack.test(stack))
                    continue;

                int reservationCount = Math.min(requiredCount, simulatedCount);
                reservations.put(i, reservations.getOrDefault(i, 0) + reservationCount);
                requiredCount -= reservationCount;
                if (requiredCount == 0)
                    break;
            }

            if (requiredCount > 0)
                return false;
        }

        return true;
    }

    public static List<Integer> getCounts(Inventory inventory, List<PredicateItemStack> predicateItemStacks, boolean existing) {
        List<Integer> stacks = new ArrayList<>();

        Map<Integer, Integer> reservations = new HashMap<>();
        for (PredicateItemStack predicateItemStack : predicateItemStacks) {
            int requiredCount = predicateItemStack.count();
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                int simulatedCount = stack.getCount() - reservations.getOrDefault(i, 0);

                if (simulatedCount == 0 || !predicateItemStack.test(stack))
                    continue;

                int reservationCount = Math.min(requiredCount, simulatedCount);
                reservations.put(i, reservations.getOrDefault(i, 0) + reservationCount);
                requiredCount -= reservationCount;
                if (requiredCount == 0)
                    break;
            }

            stacks.add(existing ? predicateItemStack.count() - requiredCount : requiredCount);
        }

        return stacks;
    }

    public static List<Integer> getExistingCounts(Inventory inventory, List<PredicateItemStack> predicateItemStacks) {
        return getCounts(inventory, predicateItemStacks, true);
    }

    public static List<Integer> getMissingCounts(Inventory inventory, List<PredicateItemStack> predicateItemStacks) {
        return getCounts(inventory, predicateItemStacks, false);
    }
    //#endregion

    public static SetStackResult getSetStackResult(Inventory inventory, int slot, ItemStack stack) {
        ItemStack currentStack = inventory.getStack(slot);
        boolean itemsAndComponentsAreEqual = ItemStack.areItemsAndComponentsEqual(currentStack, stack);
        int currentCount = currentStack.getCount();
        int newCount = stack.getCount();

        if (itemsAndComponentsAreEqual && currentCount == newCount)
            return SetStackResult.NONE;

        if (!itemsAndComponentsAreEqual && currentCount != 0 && newCount != 0)
            return SetStackResult.REPLACE;

        return newCount > currentCount ? SetStackResult.ADD : SetStackResult.REMOVE;
    }

    //#region Adding
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

            copies[i] = stack.copyWithCount(slotStack.getCount() + addCount);
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

            inventory.setStack(i, stack.copyWithCount(slotStack.getCount() + addCount));
            stack.decrement(addCount);

            if (stack.getCount() == 0)
                return;
        }
    }

    public static int add(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty())
            return 0;

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
    //#endregion
}
