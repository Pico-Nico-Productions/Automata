package pro.piconico.automata.network.message;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamDeleteC2SMessage(UUID uuid) {
    public static void handle(BotTeamDeleteC2SMessage message, ServerAccess serverAccess) {
        Optional<BotTeam> team = BotTeamPersistentState.removeTeam(message.uuid);

        if (team.isEmpty())
            return;

        BotTeamsSyncS2CMessage response = new BotTeamsSyncS2CMessage(new ArrayList<>(BotTeamPersistentState.getTeams()));
        AutomataMessages.CHANNEL.serverHandle(serverAccess.player()).send(response);
    }
}
