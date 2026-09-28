package pro.piconico.automata.entity.ai.goal;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.device.LogisticStorage;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;

public class BotStoreItemsGoal extends GoToNearestTargetGoal<BotEntity, LogisticStorage<?>> {
    public BotStoreItemsGoal(BotEntity bot, double speed, int interactDistance) {
        super(bot, speed, interactDistance);
    }

    @Override
    protected Stream<LogisticStorage<?>> streamTargets() {
        return BotNetworkManager.streamLogisticStoragesNear(getServerWorld(entity), entity.getTeamUuid(), entity.getBlockPos());
    }

    @Override
    protected boolean isValidTarget(LogisticStorage<?> target) {
        return InventoryUtils.canAdd(target, entity) > 0;
    }

    @Override
    protected BlockPos getPos(LogisticStorage<?> target) {
        return target.getPos();
    }

    @Override
    public boolean canStart() {
        return !entity.isEmpty() && super.canStart();
    }

    @Override
    public void tick() {
        super.tick();

        if (!reachedDesiredDistance())
            return;

        InventoryUtils.add(target.get(), entity);
        target = Optional.empty();
    }
}
