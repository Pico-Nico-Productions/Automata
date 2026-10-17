package pro.piconico.automata.bot.device;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.ListInventory;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.Automata;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.inventory.InventoryUtils.ItemStackMutation;
import pro.piconico.automata.util.MapUtils;

public interface LogisticStorage<T> extends BotDevice<T>, ListInventory {
    public class Reservation {
        public static final Codec<Reservation> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
                Uuids.CODEC.fieldOf("entity_uuid").forGetter(reservation -> reservation.entityUuid), //
                Codec.INT.fieldOf("slot").forGetter(reservation -> reservation.slot), //
                Codec.INT.fieldOf("count").forGetter(Reservation::getCount) //
        ).apply(instance, Reservation::new));

        public final UUID entityUuid;
        public final int slot;
        private int count;

        private static void validateCount(int count) {
            if (count < 1)
                throw new IllegalArgumentException("Must reserve at least 1");
        }

        private Reservation(UUID entityUuid, int slot, int count) {
            validateCount(count);

            this.entityUuid = entityUuid;
            this.slot = slot;
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        private void setCount(int count) {
            validateCount(count);

            this.count = count;
        }

        private void incrementCount(int amount) {
            setCount(count + amount);
        }

        private void decrementCount(int amount) {
            setCount(count - amount);
        }
    }

    @FunctionalInterface
    public interface MutateInventoryStack {
        void onChanged(ServerWorld serverWorld, LogisticStorage<?> logisticStorage, List<StackWithSlot> oldSlotStacks, ItemStackMutation mutation);
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
    public default void clear() {
        if (getFilledSlotCount() == 0)
            return;

        DefaultedList<ItemStack> heldStacks = getHeldStacks();
        List<StackWithSlot> map = IntStream.range(0, heldStacks.size()).filter(i -> !heldStacks.get(i).isEmpty()).boxed() //
                .map(i -> new StackWithSlot(i, heldStacks.get(i).copy())).toList();

        getHeldStacks().clear();
        getReservations().clear();
        markDirty();

        if (!(getWorld() instanceof ServerWorld serverWorld))
            return;

        INVENTORY_STACK_CHANGED.invoker().onChanged(serverWorld, this, map, ItemStackMutation.REMOVE);
    }

    @Override
    public default ItemStack removeStack(int slot, int amount) {
        DefaultedList<ItemStack> heldStacks = getHeldStacks();
        ItemStack oldStack = heldStacks.get(slot).copy();
        ItemStack removedStack = Inventories.splitStack(heldStacks, slot, amount);

        if (removedStack.isEmpty())
            return ItemStack.EMPTY;

        capReservations(slot, oldStack.getCount() - removedStack.getCount());
        this.markDirty();

        if (!(getWorld() instanceof ServerWorld serverWorld))
            return removedStack;

        INVENTORY_STACK_CHANGED.invoker().onChanged(serverWorld, this, List.of(new StackWithSlot(slot, oldStack)), ItemStackMutation.REMOVE);

        return removedStack;
    }

    @Override
    public default ItemStack removeStack(int slot) {
        return removeStack(slot, getMaxCount(getStack(slot)));
    }

    @Override
    public default void setStack(int slot, ItemStack stack) {
        Optional<ItemStackMutation> mutation = InventoryUtils.getSetStackMutation(this, slot, stack);

        if (mutation.isEmpty())
            return;


        ItemStack oldStack = getHeldStacks().get(slot).copy();

        setStackNoMarkDirty(slot, stack);
        if (mutation.get() == ItemStackMutation.REMOVE || mutation.get() == ItemStackMutation.REPLACE) {
            capReservations(slot, mutation.get() == ItemStackMutation.REMOVE ? stack.getCount() : 0);
        }
        this.markDirty();

        if (!(getWorld() instanceof ServerWorld serverWorld))
            return;

        INVENTORY_STACK_CHANGED.invoker().onChanged(serverWorld, this, List.of(new StackWithSlot(slot, oldStack)), mutation.get());
    }

    public Map<UUID, Map<Integer, Reservation>> getReservations();

    public default Collection<Reservation> getReservations(UUID entityUuid) {
        return getReservations().getOrDefault(entityUuid, Map.of()).values();
    }

    public default DefaultedList<Integer> getReservationList() {
        DefaultedList<Integer> reservationList = DefaultedList.ofSize(size(), 0);

        for (Map<Integer, Reservation> entityReservations : getReservations().values()) {
            for (Reservation reservation : entityReservations.values()) {
                reservationList.set(reservation.slot, reservationList.get(reservation.slot) + reservation.count);
            }
        }

        return reservationList;
    }

    public default void reserve(UUID entityUuid, int slot, int count) {
        if (count < 0) {
            Automata.logError("Reservation count was negative", IllegalArgumentException::new);
            return;
        }

        MapUtils.<Reservation>computeNested(getReservations(), (key, reservation) -> {
            if ((reservation != null ? reservation.count : 0) + count > getStack(slot).getCount()) {
                Automata.logError("Reservation exceeded stack count", IllegalStateException::new);
            }

            if (reservation == null)
                return new Reservation(entityUuid, slot, count);

            reservation.incrementCount(count);
            return reservation;
        }, entityUuid, slot);
    }

    default void unreserve(UUID entityUuid, int slot, int count) {
        if (count < 0) {
            Automata.logError("Unreservation count was negative", IllegalArgumentException::new);
            return;
        }

        Map<UUID, Map<Integer, Reservation>> reservations = getReservations();
        MapUtils.<Reservation>getNested(reservations, entityUuid, slot).ifPresent(reservation -> {
            if (count > reservation.count) {
                Automata.logError("Reservation went below 0", IllegalStateException::new);
            }

            if (count >= reservation.count) {
                MapUtils.removeNested(reservations, entityUuid, slot);
            }
            else {
                reservation.decrementCount(count);
            }
        });
    }

    default void unreserve(UUID entityUuid) {
        getReservations().remove(entityUuid);
    }

    private void capReservations(int slot, int maxCount) {
        Collection<Map<Integer, Reservation>> reservations = getReservations().values();
        int slotReservationSurplusCount = reservations.stream().mapToInt(map -> map.containsKey(slot) ? map.get(slot).count : 0).sum() - maxCount;
        for (java.util.Iterator<Map<Integer, Reservation>> iterator = reservations.iterator(); iterator.hasNext() && slotReservationSurplusCount > 0;) {
            Map<Integer, Reservation> entityReservations = iterator.next();

            if (!entityReservations.containsKey(slot))
                continue;

            Reservation reservation = entityReservations.get(slot);
            int removeCount = Math.min(reservation.count, slotReservationSurplusCount);
            if (removeCount == reservation.count) {
                entityReservations.remove(slot);
                if (entityReservations.isEmpty()) {
                    iterator.remove();
                }
            }
            else {
                reservation.decrementCount(removeCount);
            }
            slotReservationSurplusCount -= removeCount;
        }
    }
}
