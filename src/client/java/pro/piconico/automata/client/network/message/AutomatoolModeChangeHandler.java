package pro.piconico.automata.client.network.message;

import io.wispforest.owo.network.ClientAccess;
import net.minecraft.item.ItemStack;
import pro.piconico.automata.bot.mode.AutomatoolMode;
import pro.piconico.automata.client.gui.screen.BotDeviceScreen;
import pro.piconico.automata.component.AutomatoolModeComponent;
import pro.piconico.automata.network.message.AutomatoolModeChangeMessage;
import pro.piconico.automata.registry.AutomataRegistries;

public class AutomatoolModeChangeHandler {
    public static void handle(AutomatoolModeChangeMessage message, ClientAccess clientAccess) {
        ItemStack stack = clientAccess.player().getInventory().getStack(message.slot());
        AutomatoolMode mode = AutomataRegistries.AUTOMATOOL_MODE.get(message.modeId());
        AutomatoolModeComponent.set(stack, mode);

        if (clientAccess.runtime().currentScreen instanceof BotDeviceScreen botDeviceScreen) {
            botDeviceScreen.rebuildTab();
        }
    }
}
