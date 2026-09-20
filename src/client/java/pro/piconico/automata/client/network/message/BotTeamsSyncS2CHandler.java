package pro.piconico.automata.client.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ClientAccess;
import net.minecraft.client.MinecraftClient;
import pro.piconico.automata.client.gui.screen.BotDeviceScreen;
import pro.piconico.automata.network.message.BotTeamsSyncS2CMessage;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public class BotTeamsSyncS2CHandler {
    public static void handle(BotTeamsSyncS2CMessage message, ClientAccess clientAccess) {
        if (!(clientAccess.player().currentScreenHandler instanceof BotDeviceScreenHandler deviceScreenHandler))
            return;

        deviceScreenHandler.teams.clear();
        deviceScreenHandler.teams.addAll(message.teams());

        Optional<UUID> teamUuid = deviceScreenHandler.botDevice.getTeamUuid();
        if (teamUuid.isPresent() && deviceScreenHandler.teams.stream().noneMatch(team -> team.UUID.equals(teamUuid.get()))) {
            deviceScreenHandler.botDevice.setTeamUuid(Optional.empty());
        }

        if (!(MinecraftClient.getInstance().currentScreen instanceof BotDeviceScreen<?> deviceScreen))
            return;

        deviceScreen.rebuildTab();
    }
}
