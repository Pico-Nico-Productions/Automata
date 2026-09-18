package pro.piconico.automata.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import pro.piconico.automata.item.BotItem;

public class BotSlot extends DisableableSlot {
    public BotSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem() instanceof BotItem;
    }
}
