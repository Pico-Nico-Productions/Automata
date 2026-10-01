package pro.piconico.automata.bot.device;

import java.util.Optional;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.ListInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.inventory.InventoryUtils.SetStackResult;

public interface LogisticStorage<T> extends BotDevice<T>, ListInventory {
    public enum Mutation {
        ADD, REMOVE, REPLACE;

        private static Optional<Mutation> from(SetStackResult setStackResult) {
            return Optional.ofNullable(switch (setStackResult) {
                case NONE -> null;
                case REPLACE -> REPLACE;
                case ADD -> ADD;
                case REMOVE -> REMOVE;
            });
        }
    }

    @FunctionalInterface
    public interface MutateInventoryStack {
        void onChanged(World world, LogisticStorage<?> logisticStorage, Optional<Integer> slot, Mutation mutation);
    }

    public static final Event<MutateInventoryStack> INVENTORY_STACK_CHANGED = EventFactory.createArrayBacked(MutateInventoryStack.class,
            callbacks -> (world, logisticStorage, slot, mutation) -> {
                for (MutateInventoryStack callback : callbacks) {
                    callback.onChanged(world, logisticStorage, slot, mutation);
                }
            });

    public World getWorld();

    public BlockPos getPos();

    @Override
    default void clear() {
        if (getFilledSlotCount() == 0)
            return;

        this.getHeldStacks().clear();
        markDirty();
        INVENTORY_STACK_CHANGED.invoker().onChanged(getWorld(), this, Optional.empty(), Mutation.REMOVE);
    }

    @Override
    default ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.getHeldStacks(), slot, amount);

        if (!itemStack.isEmpty()) {
            this.markDirty();
            INVENTORY_STACK_CHANGED.invoker().onChanged(getWorld(), this, Optional.of(slot), Mutation.REMOVE);
        }

        return itemStack;
    }

    @Override
    default ItemStack removeStack(int slot) {
        return removeStack(slot, this.getMaxCountPerStack());
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        Optional<Mutation> mutation = Mutation.from(InventoryUtils.getSetStackResult(this, slot, stack));

        if (mutation.isEmpty())
            return;

        setStackNoMarkDirty(slot, stack);
        this.markDirty();
        INVENTORY_STACK_CHANGED.invoker().onChanged(getWorld(), this, Optional.of(slot), mutation.get());
    }
}
