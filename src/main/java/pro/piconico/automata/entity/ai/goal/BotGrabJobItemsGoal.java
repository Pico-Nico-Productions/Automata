package pro.piconico.automata.entity.ai.goal;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.bot.device.LogisticStorage;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;

public class BotGrabJobItemsGoal extends GoToNearestTargetGoal<BotEntity, LogisticStorage<?>> {
    public BotGrabJobItemsGoal(BotEntity bot, double speed, int desiredDistance) {
        super(bot, speed, desiredDistance);
    }

    @Override
    protected Stream<LogisticStorage<?>> streamTargets() {
        return BotNetworkManager.streamLogisticStoragesNear(getServerWorld(entity), entity.getTeamUuid(), entity.getBlockPos());
    }

    private Set<Predicate<ItemStack>> getStackPredicatesToGrab(boolean requiredOnly) {
        BotJob job = entity.getJob().get();
        World world = entity.getEntityWorld();
        Set<Predicate<ItemStack>> stackPredicatesToGrab = new HashSet<>(requiredOnly ? job.getRequiredStackPredicates(world) : job.getPreferredStackPredicates(world));
        if (stackPredicatesToGrab.isEmpty())
            return Set.of();

        stackPredicatesToGrab.removeIf(predicate -> entity.containsAny(predicate));
        return stackPredicatesToGrab;
    }

    private Set<Predicate<ItemStack>> getStackPredicatesToGrab() {
        return getStackPredicatesToGrab(false);
    }

    @Override
    protected boolean isValidTarget(LogisticStorage<?> target) {
        return getStackPredicatesToGrab().stream().anyMatch(predicate -> target.containsAny(predicate));
    }

    @Override
    protected BlockPos getPos(LogisticStorage<?> target) {
        return target.getPos();
    }

    @Override
    public boolean canStart() {
        if (!entity.hasEmptyStack() || entity.getJob().isEmpty())
            return false;

        return super.canStart() || !getStackPredicatesToGrab(true).isEmpty();
    }

    @Override
    public void tick() {
        if (target.isEmpty()) {
            stop();
            return;
        }

        super.tick();

        if (!reachedDesiredDistance())
            return;

        for (Predicate<ItemStack> predicate : getStackPredicatesToGrab()) {
            if (!entity.hasEmptyStack())
                break;

            Optional<Integer> slot = InventoryUtils.getSlot(target.get(), predicate);
            if (slot.isEmpty())
                continue;

            ItemStack stack = target.get().removeStack(slot.get(), 1);
            InventoryUtils.add(entity, stack);
        }
        target = Optional.empty();
    }
}
