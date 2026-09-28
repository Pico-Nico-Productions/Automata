package pro.piconico.automata.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import pro.piconico.automata.entity.BotEntity;

public class BotHoverGoal extends Goal {
    private final BotEntity bot;

    public BotHoverGoal(BotEntity bot) {
        this.bot = bot;
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public void start() {
        bot.getNavigation().stop();
    }
}
