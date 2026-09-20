package pro.piconico.automata.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import io.wispforest.owo.network.OwoNetChannel.ServerHandle;
import net.minecraft.server.network.ServerPlayerEntity;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.network.BotSyncManager;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public record BotTeamSelectMessage(Optional<UUID> teamUuid) {
    public static void handle(BotTeamSelectMessage message, ServerAccess serverAccess) {
        ServerPlayerEntity serverPlayer = serverAccess.player();

        if (!(serverPlayer.currentScreenHandler instanceof BotDeviceScreenHandler deviceScreenHandler))
            return;

        BotDevice<?> botDevice = deviceScreenHandler.botDevice;
        Optional<UUID> teamUuid = message.teamUuid();
        botDevice.setTeamUuid(teamUuid);

        ServerHandle serverHandle;
        if (deviceScreenHandler.botDevice instanceof BlockBotDevice blockBotDevice) {
            serverHandle = AutomataMessages.CHANNEL.serverHandle(blockBotDevice);
        }
        else {
            serverHandle = AutomataMessages.CHANNEL.serverHandle(serverPlayer);
        }
        serverHandle.send(message);

        if (!(botDevice instanceof ItemBotDevice))
            return;

        BotSyncManager.updateSubscription(serverPlayer, teamUuid);
    }
}
