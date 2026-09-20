package pro.piconico.automata.client.gui.screen;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
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
            FlowLayout headerRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
            headerRow.horizontalAlignment(HorizontalAlignment.CENTER);
            headerRow.child(UIComponents.label(getName(handler)).shadow(true));
            parent.child(headerRow);
        }
    }

    @Override
    protected BotDeviceHomeTab getHomeTab() {
        return AutomatoolTab.INSTANCE;
    }
}
