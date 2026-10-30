package pro.piconico.automata.client.gui.screen;

import java.util.List;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import pro.piconico.automata.client.ui.component.AutomataUIComponents;
import pro.piconico.automata.client.ui.tab.Tab;
import pro.piconico.automata.client.ui.tab.bot.BotDeviceHomeTab;
import pro.piconico.automata.client.ui.tab.bot.BotTeamSelectTab;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.screen.BotScreenHandler;
import pro.piconico.automata.screen.slot.DisableableSlot;

public class BotScreen extends BotDeviceScreen<BotScreenHandler> {
    public BotScreen(BotScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    private static class BotTab implements BotDeviceHomeTab {
        public static final BotTab INSTANCE = new BotTab();

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
        return BotTab.INSTANCE;
    }

    @Override
    protected List<Tab<BotDeviceScreenHandler>> getTabs() {
        return super.getTabs().stream().filter(tab -> tab != BotTeamSelectTab.INSTANCE).toList();
    }
}
