package pro.piconico.automata.item;

import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.registry.AutomataBots;

public class ConstructionBotItem extends BotItem {
    public ConstructionBotItem(Settings settings) {
        super(settings);
    }

    @Override
    public BotType getBotType() {
        return AutomataBots.CONSTRUCTION_BOT;
    }
}
