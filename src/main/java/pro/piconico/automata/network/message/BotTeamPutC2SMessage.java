package pro.piconico.automata.network.message;

import java.util.Optional;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamPutC2SMessage(BotTeam team) {
    public static void handle(BotTeamPutC2SMessage message, ServerAccess serverAccess) {
        Optional<BotTeam> team = BotTeamPersistentState.putTeam(message.team);

        if (team.isEmpty())
            return;

        AutomataMessages.CHANNEL.serverHandle(serverAccess.player()).send(new BotTeamsSyncS2CMessage());
    }
}
