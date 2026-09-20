package pro.piconico.automata.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class DisableableSlot extends Slot {
    private boolean enabled = true;

    public DisableableSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static boolean setEnabled(Slot slot, boolean enabled) {
        if (!(slot instanceof DisableableSlot disableableSlot))
            return false;

        disableableSlot.setEnabled(enabled);

        return true;
    }
}
