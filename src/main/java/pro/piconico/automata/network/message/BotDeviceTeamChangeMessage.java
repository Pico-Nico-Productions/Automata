package pro.piconico.automata.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.device.BotDevice;

public record BotDeviceTeamChangeMessage(BotDevice.Id deviceId, Optional<UUID> teamUuid) {
    public static void handle(BotDeviceTeamChangeMessage message, ServerAccess serverAccess) {
        Optional<BotDevice<?>> device = BotDevice.resolve(serverAccess.player(), message.deviceId);

        if (device.isEmpty())
            return;

        device.get().setTeamUuid(message.teamUuid());
    }
}
