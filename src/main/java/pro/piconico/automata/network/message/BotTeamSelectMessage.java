package pro.piconico.automata.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.server.network.ServerPlayerEntity;
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

        BotDevice<?> device = deviceScreenHandler.device;
        Optional<UUID> teamUuid = message.teamUuid();
        device.setTeamUuid(teamUuid);

        AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(serverPlayer).send(message);

        if (!(device instanceof ItemBotDevice))
            return;

        BotSyncManager.updateSubscription(serverPlayer, teamUuid);
    }
}
