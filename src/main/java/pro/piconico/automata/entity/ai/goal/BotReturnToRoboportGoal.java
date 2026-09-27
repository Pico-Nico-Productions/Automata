package pro.piconico.automata.entity.ai.goal;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;

public class BotReturnToRoboportGoal extends Goal {
    private final BotEntity bot;
    private final double speed;
    private final int interactDistance;
    private Optional<RoboportBlockEntity> roboport = Optional.empty();

    public BotReturnToRoboportGoal(BotEntity bot, double speed, int interactDistance) {
        this.bot = bot;
        this.speed = speed;
        this.interactDistance = interactDistance;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    private boolean isValidRoboport(RoboportBlockEntity roboport) {
        return InventoryUtils.canAdd(roboport, bot.getBotType().item());
    }

    @Override
    public boolean canStart() {
        roboport = BotNetworkManager.getRoboportFor((ServerWorld)bot.getEntityWorld(), bot);

        return roboport.isPresent();
    }

    private void startNavigation() {
        BlockPos pos = roboport.get().getPos();
        bot.getNavigation().startMovingTo(pos.getX(), pos.getY(), pos.getZ(), speed);
    }

    @Override
    public void start() {
        startNavigation();
    }

    @Override
    public void stop() {
        roboport = Optional.empty();
    }

    @Override
    public boolean shouldContinue() {
        return roboport.map(this::isValidRoboport).isPresent() || canStart();
    }

    @Override
    public void tick() {
        BlockPos roboportPos = roboport.get().getPos();

        if (!bot.getNavigation().getTargetPos().equals(roboportPos)) {
            startNavigation();
        }

        if (bot.getBlockPos().getChebyshevDistance(roboportPos) > interactDistance)
            return;

        roboport.get().tryAdd(bot);
    }
}
