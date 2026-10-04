package pro.piconico.automata.entity.ai.goal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.bot.device.LogisticStorage;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.item.ItemUtils.PredicateItemStack;

public class BotGrabJobItemsGoal extends GoToNearestTargetGoal<BotEntity, LogisticStorage<?>> {
    public BotGrabJobItemsGoal(BotEntity bot, double speed, int desiredDistance) {
        super(bot, speed, desiredDistance);
    }

    @Override
    protected Stream<LogisticStorage<?>> streamTargets() {
        return BotNetworkManager.streamLogisticStoragesNear(getServerWorld(entity), entity.getTeamUuid(), entity.getBlockPos());
    }

    private List<PredicateItemStack> getMissingStackPredicates(boolean requiredOnly) {
        BotJob job = entity.getJob().get();
        World world = entity.getEntityWorld();
        List<PredicateItemStack> stacks = requiredOnly ? job.getRequiredStacks(world) : job.getPreferredStacks(world);

        if (stacks.isEmpty())
            return List.of();

        List<Integer> missingCounts = InventoryUtils.getMissingCounts(entity, stacks);
        List<PredicateItemStack> missingStacks = new ArrayList<>();

        for (int i = 0; i < stacks.size(); i++) {
            int count = missingCounts.get(i);

            if (count == 0)
                continue;

            missingStacks.add(new PredicateItemStack(stacks.get(i).predicate(), missingCounts.get(i)));
        }

        return missingStacks;
    }

    private List<PredicateItemStack> getStackPredicatesToGrab() {
        return getMissingStackPredicates(false);
    }

    @Override
    protected boolean isValidTarget(LogisticStorage<?> target) {
        return getStackPredicatesToGrab().stream().anyMatch(predicate -> target.containsAny(predicate.predicate()));
    }

    @Override
    protected BlockPos getPos(LogisticStorage<?> target) {
        return target.getPos();
    }

    @Override
    public boolean canStart() {
        if (!entity.hasEmptyStack() || entity.getJob().isEmpty())
            return false;

        return super.canStart() || !getMissingStackPredicates(true).isEmpty();
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

        for (PredicateItemStack predicate : getStackPredicatesToGrab()) {
            if (!entity.hasEmptyStack())
                break;

            Optional<Integer> slot = InventoryUtils.getSlot(target.get(), predicate.predicate());
            if (slot.isEmpty())
                continue;

            ItemStack stack = target.get().removeStack(slot.get(), 1);
            InventoryUtils.add(entity, stack);
        }
        target = Optional.empty();
    }
}
