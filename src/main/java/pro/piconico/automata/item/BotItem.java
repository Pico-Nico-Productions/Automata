package pro.piconico.automata.item;

import net.minecraft.item.Item;
import pro.piconico.automata.bot.BotType;

public abstract class BotItem extends Item {
    public BotItem(Settings settings) {
        super(settings.maxCount(16));
    }

    public abstract BotType getBotType();
}
