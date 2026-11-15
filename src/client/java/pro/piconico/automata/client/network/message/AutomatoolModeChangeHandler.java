package pro.piconico.automata.client.network.message;

import java.util.Optional;
import io.wispforest.owo.network.ClientAccess;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.bot.device.ItemBotDevice.ItemId;
import pro.piconico.automata.bot.mode.AutomatoolMode;
import pro.piconico.automata.client.gui.screen.BotDeviceScreen;
import pro.piconico.automata.component.AutomatoolModeComponent;
import pro.piconico.automata.network.message.AutomatoolModeChangeMessage;
import pro.piconico.automata.registry.AutomataRegistries;

public class AutomatoolModeChangeHandler {
    public static void handle(AutomatoolModeChangeMessage message, ClientAccess clientAccess) {
        Optional<ItemBotDevice> automatool = ItemBotDevice.resolve(clientAccess.player(), new ItemId(message.slot()));

        if (automatool.isEmpty())
            return;

        AutomatoolMode mode = AutomataRegistries.AUTOMATOOL_MODE.get(message.modeId());

        if (mode == null)
            return;

        AutomatoolModeComponent.set(automatool.get().getStack(), mode);

        if (clientAccess.runtime().currentScreen instanceof BotDeviceScreen botDeviceScreen) {
            botDeviceScreen.rebuildTab();
        }
    }
}
