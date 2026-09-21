package pro.piconico.automata.client.network.message;

import io.wispforest.owo.network.ClientAccess;
import pro.piconico.automata.network.message.BotTeamSelectMessage;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

// TODO: Make more robust so clients who receive the message after closing the screen still sync the change (Add UUID to devices)
public class BotTeamSelectHandler {
    public static void handle(BotTeamSelectMessage message, ClientAccess clientAccess) {
        if (!(clientAccess.player().currentScreenHandler instanceof BotDeviceScreenHandler deviceScreenHandler))
            return;

        deviceScreenHandler.device.setTeamUuid(message.teamUuid());
    }
}
