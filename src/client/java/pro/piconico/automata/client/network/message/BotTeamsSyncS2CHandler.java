package pro.piconico.automata.client.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ClientAccess;
import pro.piconico.automata.network.message.BotTeamsSyncS2CMessage;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

// TODO: Make more robust so clients who receive the message after closing the screen still sync the change (Add UUID to devices)
public class BotTeamsSyncS2CHandler {
    public static void handle(BotTeamsSyncS2CMessage message, ClientAccess clientAccess) {
        if (!(clientAccess.player().currentScreenHandler instanceof BotDeviceScreenHandler deviceScreenHandler))
            return;

        deviceScreenHandler.teams.clear();
        deviceScreenHandler.teams.addAll(message.teams());

        Optional<UUID> teamUuid = deviceScreenHandler.device.getTeamUuid();
        if (teamUuid.isPresent() && deviceScreenHandler.teams.stream().noneMatch(team -> team.UUID.equals(teamUuid.get()))) {
            deviceScreenHandler.device.setTeamUuid(Optional.empty());
        }
    }
}
