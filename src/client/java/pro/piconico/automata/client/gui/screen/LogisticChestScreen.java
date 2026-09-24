package pro.piconico.automata.client.gui.screen;

import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import pro.piconico.automata.client.ui.component.AutomataUIComponents;
import pro.piconico.automata.client.ui.tab.BotDeviceHomeTab;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.screen.LogisticChestScreenHandler;
import pro.piconico.automata.screen.slot.DisableableSlot;

public class LogisticChestScreen extends BotDeviceScreen<LogisticChestScreenHandler> {
    public LogisticChestScreen(LogisticChestScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    private static class LogisticChestTab implements BotDeviceHomeTab {
        public static final LogisticChestTab INSTANCE = new LogisticChestTab();

        @Override
        public void build(BotDeviceScreenHandler handler, FlowLayout parent) {
            parent.child(AutomataUIComponents.centerHeader(getName(handler)));

            buildDeviceAndPlayerSlots(handler, parent);

            for (Slot slot : handler.slots) {
                DisableableSlot.setEnabled(slot, true);
            }
        }
    }

    @Override
    protected BotDeviceHomeTab getHomeTab() {
        return LogisticChestTab.INSTANCE;
    }
}
