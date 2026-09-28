package pro.piconico.automata.entity.ai.goal;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;

public class BotPickUpItemsGoal extends GoToNearestTargetGoal<BotEntity, ItemEntity> {
    private final Inventory inventory;

    public BotPickUpItemsGoal(BotEntity bot, Inventory inventory, double speed, int interactDistance) {
        super(bot, speed, interactDistance);
        this.inventory = inventory;
    }

    @Override
    protected Stream<ItemEntity> streamTargets() {
        return entity.getItemsToPickUp().stream();
    }

    @Override
    protected boolean isValidTarget(ItemEntity item) {
        return item.isAlive() && InventoryUtils.canAdd(inventory, item.getStack()) > 0;
    }

    @Override
    protected BlockPos getPos(ItemEntity target) {
        return target.getBlockPos();
    }

    @Override
    public void stop() {
        entity.removeInvalidItemsToPickUp();
    }

    @Override
    public void tick() {
        super.tick();

        if (!reachedDesiredDistance())
            return;

        ItemStack stack = target.get().getStack();
        InventoryUtils.add(inventory, stack);

        if (stack.isEmpty()) {
            target.get().discard();
        }
        target = Optional.empty();
    }
}
