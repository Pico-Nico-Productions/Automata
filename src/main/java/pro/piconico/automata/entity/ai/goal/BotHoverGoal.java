package pro.piconico.automata.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;

public class BotHoverGoal extends Goal {
    public BotHoverGoal() {
    }

    @Override
    public boolean canStart() {
        return true;
    }
}
