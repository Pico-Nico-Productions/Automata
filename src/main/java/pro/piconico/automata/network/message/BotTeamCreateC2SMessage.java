package pro.piconico.automata.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeam.BotTeamSettings;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamCreateC2SMessage(BotTeamSettings settings, UUID ownerUuid) {
    public static void handle(BotTeamCreateC2SMessage message, ServerAccess serverAccess) {
        Optional<BotTeam> team = BotTeamPersistentState.addTeam(message.settings, Optional.of(message.ownerUuid));

        if (team.isEmpty())
            return;

        BotTeamsSyncS2CMessage.syncToPlayersUsingTeams(serverAccess.runtime());
    }
}
