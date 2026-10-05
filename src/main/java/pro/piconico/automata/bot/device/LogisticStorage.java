package pro.piconico.automata.bot.device;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.ListInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.inventory.InventoryUtils.ItemStackMutation;

public interface LogisticStorage<T> extends BotDevice<T>, ListInventory {
    public record SlotStack(int slot, ItemStack stack) {
    }

    @FunctionalInterface
    public interface MutateInventoryStack {
        void onChanged(ServerWorld serverWorld, LogisticStorage<?> logisticStorage, List<SlotStack> oldSlotStacks, ItemStackMutation mutation);
    }

    public static final Event<MutateInventoryStack> INVENTORY_STACK_CHANGED = EventFactory.createArrayBacked(MutateInventoryStack.class,
            callbacks -> (serverWorld, logisticStorage, slotStacks, mutation) -> {
                for (MutateInventoryStack callback : callbacks) {
                    callback.onChanged(serverWorld, logisticStorage, slotStacks, mutation);
                }
            });

    public World getWorld();

    public BlockPos getPos();

    @Override
    default void clear() {
        if (getFilledSlotCount() == 0)
            return;

        DefaultedList<ItemStack> heldStacks = getHeldStacks();
        List<SlotStack> map = IntStream.range(0, heldStacks.size()).filter(i -> !heldStacks.get(i).isEmpty()).boxed() //
                .map(i -> new SlotStack(i, heldStacks.get(i).copy())).toList();

        getHeldStacks().clear();
        markDirty();

        if (!(getWorld() instanceof ServerWorld serverWorld))
            return;

        INVENTORY_STACK_CHANGED.invoker().onChanged(serverWorld, this, map, ItemStackMutation.REMOVE);
    }

    @Override
    default ItemStack removeStack(int slot, int amount) {
        DefaultedList<ItemStack> heldStacks = getHeldStacks();
        ItemStack oldStack = heldStacks.get(slot).copy();
        ItemStack itemStack = Inventories.splitStack(heldStacks, slot, amount);

        if (itemStack.isEmpty())
            return ItemStack.EMPTY;

        this.markDirty();

        if (!(getWorld() instanceof ServerWorld serverWorld))
            return itemStack;

        INVENTORY_STACK_CHANGED.invoker().onChanged(serverWorld, this, List.of(new SlotStack(slot, oldStack)), ItemStackMutation.REMOVE);

        return itemStack;
    }

    @Override
    default ItemStack removeStack(int slot) {
        return removeStack(slot, getMaxCount(getStack(slot)));
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        Optional<ItemStackMutation> mutation = InventoryUtils.getSetStackMutation(this, slot, stack);

        if (mutation.isEmpty())
            return;

        ItemStack oldStack = getHeldStacks().get(slot).copy();
        setStackNoMarkDirty(slot, stack);
        this.markDirty();

        if (!(getWorld() instanceof ServerWorld serverWorld))
            return;

        INVENTORY_STACK_CHANGED.invoker().onChanged(serverWorld, this, List.of(new SlotStack(slot, oldStack)), mutation.get());
    }
}
