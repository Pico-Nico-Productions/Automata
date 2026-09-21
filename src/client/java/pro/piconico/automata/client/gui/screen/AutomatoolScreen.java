package pro.piconico.automata.client.gui.screen;

import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import pro.piconico.automata.client.ui.component.AutomataUIComponents;
import pro.piconico.automata.client.ui.tab.BotDeviceHomeTab;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.screen.ItemBotDeviceScreenHandler;

public class AutomatoolScreen extends BotDeviceScreen<ItemBotDeviceScreenHandler> {
    public AutomatoolScreen(ItemBotDeviceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    private static class AutomatoolTab implements BotDeviceHomeTab {
        public static final AutomatoolTab INSTANCE = new AutomatoolTab();

        @Override
        public void build(BotDeviceScreenHandler handler, FlowLayout parent) {
            parent.child(AutomataUIComponents.centerHeader(getName(handler)));
        }
    }

    @Override
    protected BotDeviceHomeTab getHomeTab() {
        return AutomatoolTab.INSTANCE;
    }
}
