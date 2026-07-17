package pro.piconico.automata.item;

import net.minecraft.item.Item;

public class ConstructionBotItem extends Item {
    public ConstructionBotItem(Settings settings) {
        super(settings.maxCount(16));
    }
}
