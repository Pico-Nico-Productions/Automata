package pro.piconico.automata.client.network.message;

import java.util.Optional;
import io.wispforest.owo.network.ClientAccess;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.network.message.BotDeviceTeamChangeMessage;

public class BotDeviceTeamChangeHandler {
    public static void handle(BotDeviceTeamChangeMessage message, ClientAccess clientAccess) {
        Optional<BotDevice<?>> device = BotDevice.resolve(clientAccess.player(), message.deviceId());

        if (device.isEmpty())
            return;

        device.get().setTeamUuid(message.teamUuid());
    }
}
