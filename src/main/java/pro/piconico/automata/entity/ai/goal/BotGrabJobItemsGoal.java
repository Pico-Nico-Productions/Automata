package pro.piconico.automata.entity.ai.goal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Stream;
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
        Optional<UUID> teamUuid = entity.getTeamUuid();

        if (teamUuid.isEmpty())
            return Stream.empty();

        return BotNetworkManager.streamReservationLogisticStorages(getServerWorld(entity), entity.getTeamUuid().get(), entity.getBlockPos(), entity.getUuid());
    }

    @Override
    protected boolean isValidTarget(LogisticStorage<?> target) {
        return true;
    }

    @Override
    protected BlockPos getPos(LogisticStorage<?> target) {
        return target.getPos();
    }

    private boolean hasMissingPredicateStackItems() {
        BotJob job = entity.getJob().get();
        World world = entity.getEntityWorld();
        List<PredicateItemStack> stacks = job.getRequiredStacks(world);

        if (stacks.isEmpty())
            return false;

        return InventoryUtils.getStacks(entity, stacks, false).stream().anyMatch(predicateItemStack -> predicateItemStack.count() != 0);
    }

    @Override
    public boolean canStart() {
        if (!entity.hasEmptyStack() || entity.getJob().isEmpty())
            return false;

        return super.canStart() || hasMissingPredicateStackItems();
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

        Map<Integer, Integer> slotReservations = BotNetworkManager
                .getReservations(getServerWorld(entity), entity.getTeamUuid().get(), entity.getBlockPos(), entity.getUuid()).getOrDefault(target.get().getPos(), null);

        if (slotReservations == null) {
            target = Optional.empty();
            return;
        }

        for (Entry<Integer,Integer> slotCount : slotReservations.entrySet()) {
            LogisticStorage<?> storage = target.get();
            int slot = slotCount.getKey(), count = slotCount.getValue();
            InventoryUtils.add(entity, storage.removeStack(slot, count));
            BotNetworkManager.unreserve(getServerWorld(entity), entity.getTeamUuid().get(), storage.getPos(), slot, count, entity.getUuid());
        }
        target = Optional.empty();
    }
}
