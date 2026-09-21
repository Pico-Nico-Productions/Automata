package pro.piconico.automata.network.message;

import java.util.Optional;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamUpdateC2SMessage(BotTeam team) {
    public static void handle(BotTeamUpdateC2SMessage message, ServerAccess serverAccess) {
        Optional<BotTeam> team = BotTeamPersistentState.updateTeam(message.team);

        if (team.isEmpty())
            return;

        AutomataMessages.CHANNEL.serverHandle(serverAccess.player()).send(new BotTeamsSyncS2CMessage());
    }
}
