package pro.piconico.automata.entity.ai.goal;

import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;

public class BotReturnToRoboportGoal extends GoToNearestTargetGoal<BotEntity, RoboportBlockEntity> {
    public BotReturnToRoboportGoal(BotEntity bot, double speed, int interactDistance) {
        super(bot, speed, interactDistance);
    }

    @Override
    protected Stream<RoboportBlockEntity> streamTargets() {
        return BotNetworkManager.streamRoboportsNear(getServerWorld(entity), entity.getTeamUuid(), entity.getBlockPos());
    }

    @Override
    protected boolean isValidTarget(RoboportBlockEntity target) {
        return InventoryUtils.canAdd(target, entity.getBotType().item());
    }

    @Override
    protected BlockPos getPos(RoboportBlockEntity target) {
        return target.getPos();
    }

    @Override
    public boolean canStart() {
        return entity.isEmpty() && super.canStart();
    }

    @Override
    public void tick() {
        super.tick();

        if (!reachedDesiredDistance())
            return;

        target.get().tryAdd(entity);
    }
}
