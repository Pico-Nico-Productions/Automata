package pro.piconico.automata.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.BotEntity;

public class BotDoJobGoal extends Goal {
    private final BotEntity bot;
    private final double speed;
    private final int interactDistance;

    public BotDoJobGoal(BotEntity bot, double speed, int interactDistance) {
        this.bot = bot;
        this.speed = speed;
        this.interactDistance = interactDistance;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return bot.getJob().isPresent();
    }

    private void startNavigation() {
        BlockPos targetPos = bot.getJob().get().pos();
        bot.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speed);
    }

    @Override
    public void start() {
        startNavigation();
    }

    @Override
    public void tick() {
        BotJob job = bot.getJob().get();

        if (!bot.getNavigation().getTargetPos().equals(job.pos())) {
            startNavigation();
        }

        if (bot.getBlockPos().getChebyshevDistance(job.pos()) > interactDistance)
            return;

        bot.endJob(job.execute((ServerWorld)bot.getEntityWorld(), bot));
    }
}
