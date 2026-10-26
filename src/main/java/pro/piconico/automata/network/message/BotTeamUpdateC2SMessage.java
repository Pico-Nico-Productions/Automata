package pro.piconico.automata.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeam.BotTeamSettings;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamUpdateC2SMessage(UUID teamUuid, BotTeamSettings settings) {
    public static void handle(BotTeamUpdateC2SMessage message, ServerAccess serverAccess) {
        Optional<BotTeam> team = BotTeamPersistentState.updateTeam(message.teamUuid, message.settings);

        if (team.isEmpty())
            return;

        BotTeamsSyncS2CMessage.syncToPlayersUsingTeams(serverAccess.runtime());
    }
}
