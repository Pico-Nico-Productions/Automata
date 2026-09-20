package pro.piconico.automata.client.network.message;

import io.wispforest.owo.network.ClientAccess;
import net.minecraft.client.MinecraftClient;
import pro.piconico.automata.client.gui.screen.BotDeviceScreen;
import pro.piconico.automata.network.message.BotTeamSelectMessage;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

// TODO: Make more robust so clients who receive the message after closing the screen still sync the change (Add UUID to devices)
public class BotTeamSelectHandler {
    public static void handle(BotTeamSelectMessage message, ClientAccess clientAccess) {
        if (!(clientAccess.player().currentScreenHandler instanceof BotDeviceScreenHandler deviceScreenHandler))
            return;

        deviceScreenHandler.botDevice.setTeamUuid(message.teamUuid());

        // TODO: Replace direct screen call with screen listening to TEAM_CHANGED event
        if (!(MinecraftClient.getInstance().currentScreen instanceof BotDeviceScreen<?> deviceScreen))
            return;

        deviceScreen.rebuildTab();
    }
}
