package pro.piconico.automata.entity.ai.goal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import pro.piconico.automata.inventory.InventoryUtils;

public class PickUpItemGoal extends Goal {
    private final PathAwareEntity entity;
    private final Inventory inventory;
    private final double searchScale;
    private final double speed;
    private final double pickUpRadius;
    private Optional<ItemEntity> targetItem = Optional.empty();

    public PickUpItemGoal(PathAwareEntity entity, Inventory inventory, double searchScale, double speed, double pickUpRadius) {
        this.entity = entity;
        this.inventory = inventory;
        this.searchScale = searchScale;
        this.speed = speed;
        this.pickUpRadius = pickUpRadius;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    private boolean isValidTarget(ItemEntity item) {
        return item.isAlive() && !item.cannotPickup() && InventoryUtils.canAddCount(inventory, item.getStack()) > 0;
    }

    @Override
    public boolean canStart() {
        targetItem = entity.getEntityWorld() //
                .getEntitiesByClass(ItemEntity.class, entity.getBoundingBox().expand(searchScale), this::isValidTarget) //
                .stream().min(Comparator.comparingDouble(item -> entity.squaredDistanceTo(item)));

        return targetItem.isPresent();
    }

    private void startNavigation() {
        entity.getNavigation().startMovingTo(targetItem.get(), speed);
    }

    @Override
    public void start() {
        startNavigation();
    }

    @Override
    public void stop() {
        targetItem = Optional.empty();
    }

    @Override
    public boolean shouldContinue() {
        return targetItem.map(this::isValidTarget).isPresent() || canStart();
    }

    @Override
    public void tick() {
        if (!entity.getNavigation().getTargetPos().equals(targetItem.get().getBlockPos())) {
            startNavigation();
        }

        entity.getLookControl().lookAt(targetItem.get());
        if (entity.squaredDistanceTo(targetItem.get()) > pickUpRadius * pickUpRadius)
            return;

        ItemStack stack = targetItem.get().getStack();
        InventoryUtils.add(inventory, stack);

        if (stack.isEmpty()) {
            targetItem.get().discard();
        }
        targetItem = Optional.empty();
    }
}
