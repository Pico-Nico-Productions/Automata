package pro.piconico.automata.network.message;

import java.util.ArrayList;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamsSyncC2SMessage() {
    public static void handle(BotTeamsSyncC2SMessage message, ServerAccess serverAccess) {
        BotTeamsSyncS2CMessage response = new BotTeamsSyncS2CMessage(new ArrayList<>(BotTeamPersistentState.getTeams()));
        AutomataMessages.CHANNEL.serverHandle(serverAccess.player()).send(response);
    }
}
