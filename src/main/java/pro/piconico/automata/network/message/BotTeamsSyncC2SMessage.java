package pro.piconico.automata.network.message;

import io.wispforest.owo.network.ServerAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import pro.piconico.automata.registry.AutomataMessages;

public record BotTeamsSyncC2SMessage() {
    public static void handle(BotTeamsSyncC2SMessage message, ServerAccess serverAccess) {
        ServerPlayerEntity serverPlayer = serverAccess.player();
        AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(serverPlayer).send(new BotTeamsSyncS2CMessage(serverPlayer));
    }
}
