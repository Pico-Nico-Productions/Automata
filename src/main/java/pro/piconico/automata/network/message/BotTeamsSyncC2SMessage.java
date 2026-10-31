package pro.piconico.automata.network.message;

import io.wispforest.owo.network.ServerAccess;

public record BotTeamsSyncC2SMessage() {
    public static void handle(BotTeamsSyncC2SMessage message, ServerAccess serverAccess) {
        BotTeamsSyncS2CMessage.syncTo(serverAccess.player());
    }
}
