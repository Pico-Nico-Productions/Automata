package pro.piconico.automata.screen.slot;

import java.util.List;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import pro.piconico.automata.registry.AutomataItems;

public class BotSlot extends Slot {
    private static final List<Item> bots = List.of(AutomataItems.CONSTRUCTION_BOT);

    public BotSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return bots.stream().anyMatch(item -> stack.isOf(item));
    }
}
