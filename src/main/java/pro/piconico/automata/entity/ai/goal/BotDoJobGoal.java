package pro.piconico.automata.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.BotJob.TickResult;
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
        return bot.hasEmptyStack() && bot.getJob().isPresent();
    }

    @Override
    public void stop() {
        bot.getNavigation().stop();
    }

    @Override
    public void tick() {
        BotJob job = bot.getJob().get();

        if (bot.getNavigation().isIdle() || !job.pos().equals(bot.getNavigation().getTargetPos())) {
            BlockPos targetPos = bot.getJob().get().pos();
            bot.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speed);
        }

        if (bot.getBlockPos().getChebyshevDistance(job.pos()) > interactDistance)
            return;

        TickResult tickResult = job.tick(getServerWorld(bot), bot);

        if (tickResult == TickResult.Pending)
            return;

        bot.endJob(tickResult == TickResult.Succeeded);
    }
}
