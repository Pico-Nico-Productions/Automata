package pro.piconico.automata.client.gui.screen;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.client.ui.tab.BotDeviceHomeTab;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.screen.RoboportScreenHandler;
import pro.piconico.automata.screen.ScreenConstants;
import pro.piconico.automata.screen.slot.DisableableSlot;

public class RoboportScreen extends BotDeviceScreen<RoboportScreenHandler> {
    public RoboportScreen(RoboportScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    private static class RoboportTab implements BotDeviceHomeTab {
        public static final RoboportTab INSTANCE = new RoboportTab();

        @Override
        public void build(BotDeviceScreenHandler handler, FlowLayout parent) {
            FlowLayout headerRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
            headerRow.horizontalAlignment(HorizontalAlignment.CENTER);
            headerRow.child(UIComponents.label(getName(handler)).shadow(true));
            parent.child(headerRow);

            for (int indexX = 0; indexX < RoboportBlockEntity.BOT_SLOT_COUNT; indexX++) {
                int x = RoboportScreenHandler.BOT_BAR_X + indexX * ScreenConstants.SLOT_DELTA;
                BotDeviceScreen.buildSlot(parent, x, RoboportScreenHandler.BOT_BAR_Y);
            }

            for (int indexY = 0; indexY < 3; indexY++) {
                for (int indexX = 0; indexX < 9; indexX++) {
                    int x = RoboportScreenHandler.INVENTORY_X + indexX * ScreenConstants.SLOT_DELTA;
                    int y = RoboportScreenHandler.INVENTORY_Y + indexY * ScreenConstants.SLOT_DELTA;
                    BotDeviceScreen.buildSlot(parent, x, y);
                }
            }

            for (int indexX = 0; indexX < 9; indexX++) {
                int x = RoboportScreenHandler.INVENTORY_X + indexX * ScreenConstants.SLOT_DELTA;
                BotDeviceScreen.buildSlot(parent, x, RoboportScreenHandler.HOTBAR_Y);
            }

            for (Slot slot : handler.slots) {
                DisableableSlot.setEnabled(slot, true);
            }
        }
    }

    @Override
    protected BotDeviceHomeTab getHomeTab() {
        return RoboportTab.INSTANCE;
    }
}
