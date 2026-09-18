package pro.piconico.automata.client.gui.screen;

import io.wispforest.owo.ui.component.UIComponents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import pro.piconico.automata.screen.ItemBotDeviceScreenHandler;

public class AutomatoolScreen extends BotDeviceScreen<ItemBotDeviceScreenHandler> {
    public AutomatoolScreen(ItemBotDeviceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void buildHomeTab() {
        body.child(UIComponents.label(this.title).shadow(true));
    }
}
