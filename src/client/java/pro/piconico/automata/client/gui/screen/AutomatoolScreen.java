package pro.piconico.automata.client.gui.screen;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.bot.mode.AutomatoolMode;
import pro.piconico.automata.client.ui.component.AutomataUIComponents;
import pro.piconico.automata.client.ui.tab.bot.BotDeviceHomeTab;
import pro.piconico.automata.component.AutomatoolModeComponent;
import pro.piconico.automata.network.message.AutomatoolModeChangeMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataRegistries;
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

            ItemBotDevice itemDevice = (ItemBotDevice)handler.device;
            AutomatoolMode selectedMode = AutomatoolModeComponent.getOrDefault(itemDevice.getStack());

            FlowLayout modePanel = UIContainers.horizontalFlow(Sizing.fill(), Sizing.content());
            modePanel.horizontalAlignment(HorizontalAlignment.CENTER);
            for (AutomatoolMode mode : AutomataRegistries.AUTOMATOOL_MODE.stream().toList()) {
                ButtonComponent modeButton = UIComponents.button(mode.getName(), ignored -> {
                    AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new AutomatoolModeChangeMessage(itemDevice.slot, mode));
                });
                modeButton.active(mode != selectedMode);
                modePanel.child(modeButton);
            }
            parent.child(UIContainers.horizontalScroll(Sizing.fill(), Sizing.content(), modePanel));
        }
    }

    @Override
    protected BotDeviceHomeTab getHomeTab() {
        return AutomatoolTab.INSTANCE;
    }
}
