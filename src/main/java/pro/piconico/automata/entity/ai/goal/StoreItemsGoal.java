package pro.piconico.automata.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import pro.piconico.automata.entity.BotEntity;

public class StoreItemsGoal extends Goal {
    private final BotEntity bot;

    public StoreItemsGoal(BotEntity bot) {
        this.bot = bot;
    }

    @Override
    public boolean canStart() {
        return !bot.isEmpty();
    }
}
