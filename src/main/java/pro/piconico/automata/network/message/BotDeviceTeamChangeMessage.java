package pro.piconico.automata.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotDeviceTeamChangeMessage(BotDevice.Id deviceId, Optional<UUID> teamUuid) {
    public static void handle(BotDeviceTeamChangeMessage message, ServerAccess serverAccess) {
        ServerPlayerEntity serverPlayer = serverAccess.player();
        Optional<BotDevice<?>> device = BotDevice.resolve(serverPlayer, message.deviceId);

        if (device.isEmpty())
            return;

        Optional<BotTeam> deviceTeam = device.get().getTeamUuid().map(uuid -> BotTeamPersistentState.getTeam(uuid).orElse(null));
        if (deviceTeam.isPresent() && !deviceTeam.get().hasMemberWith(serverPlayer, BotTeamPermission.CHANGE_DEVICE_TEAM))
            return;

        if (message.teamUuid().isPresent()
                && !BotTeamPersistentState.teamExistsAndHasMemberWith(message.teamUuid.get(), serverPlayer, BotTeamPermission.CHANGE_DEVICE_TEAM))
            return;

        device.get().setTeamUuid(message.teamUuid());
    }
}
