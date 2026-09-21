package pro.piconico.automata.network.message;

import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.registry.AutomataMessages;

public record BotTeamsSyncC2SMessage() {
    public static void handle(BotTeamsSyncC2SMessage message, ServerAccess serverAccess) {
        AutomataMessages.CHANNEL.serverHandle(serverAccess.player()).send(new BotTeamsSyncS2CMessage());
    }
}
