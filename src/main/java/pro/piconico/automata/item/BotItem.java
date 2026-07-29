package pro.piconico.automata.item;

import net.minecraft.item.Item;

public abstract class BotItem extends Item {
    public BotItem(Settings settings) {
        super(settings.maxCount(16));
    }
}
