package pro.piconico.automata.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface SimpleInventory extends Inventory {
    public DefaultedList<ItemStack> getInventory();

    @Override
    public default void clear() {
        getInventory().clear();
        markDirty();
    }

    @Override
    public default int size() {
        return getInventory().size();
    }

    @Override
    public default boolean isEmpty() {
        return getInventory().stream().allMatch(itemStack -> itemStack.isEmpty());
    }

    @Override
    public default ItemStack getStack(int slot) {
        return getInventory().get(slot);
    }

    @Override
    public default ItemStack removeStack(int slot, int amount) {
        ItemStack splitStack = Inventories.splitStack(getInventory(), slot, amount);
        markDirty();

        return splitStack;
    }

    @Override
    public default ItemStack removeStack(int slot) {
        ItemStack removedStack = Inventories.removeStack(getInventory(), slot);
        markDirty();

        return removedStack;
    }

    @Override
    public default void setStack(int slot, ItemStack stack) {
        getInventory().set(slot, stack);
        markDirty();
    }

    @Override
    public default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public default boolean isValid(int slot, ItemStack stack) {
        return true;
    }
}
