package pro.piconico.automata.client.network.message;

import io.wispforest.owo.network.ClientAccess;
import pro.piconico.automata.network.message.BotTeamsSyncS2CMessage;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public class BotTeamsSyncS2CHandler {
    public static void handle(BotTeamsSyncS2CMessage message, ClientAccess clientAccess) {
        if (!(clientAccess.player().currentScreenHandler instanceof BotDeviceScreenHandler deviceScreenHandler))
            return;

        deviceScreenHandler.teams.clear();
        deviceScreenHandler.teams.addAll(message.teams());
    }
}
