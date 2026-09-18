package pro.piconico.automata.client.gui.screen;

import io.wispforest.owo.ui.component.UIComponents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.screen.RoboportScreenHandler;

public class RoboportScreen extends BotDeviceScreen<RoboportScreenHandler> {
    public RoboportScreen(RoboportScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void buildHomeTab() {
        body.child(UIComponents.label(this.title).shadow(true));

        for (int indexX = 0; indexX < RoboportBlockEntity.BOT_SLOT_COUNT; indexX++) {
            int x = RoboportScreenHandler.BOT_BAR_X + indexX * RoboportScreenHandler.SLOT_DELTA;
            buildSlot(x, RoboportScreenHandler.BOT_BAR_Y);
        }

        for (int indexY = 0; indexY < 3; indexY++) {
            for (int indexX = 0; indexX < 9; indexX++) {
                int x = RoboportScreenHandler.INVENTORY_X + indexX * RoboportScreenHandler.SLOT_DELTA;
                int y = RoboportScreenHandler.INVENTORY_Y + indexY * RoboportScreenHandler.SLOT_DELTA;
                buildSlot(x, y);
            }
        }

        for (int indexX = 0; indexX < 9; indexX++) {
            int x = RoboportScreenHandler.INVENTORY_X + indexX * RoboportScreenHandler.SLOT_DELTA;
            buildSlot(x, RoboportScreenHandler.HOTBAR_Y);
        }
    }
}
