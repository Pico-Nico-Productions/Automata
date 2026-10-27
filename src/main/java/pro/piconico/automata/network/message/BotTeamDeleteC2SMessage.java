package pro.piconico.automata.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamDeleteC2SMessage(UUID teamUuid) {
    public static void handle(BotTeamDeleteC2SMessage message, ServerAccess serverAccess) {
        if (!BotTeamPersistentState.teamHasMemberWith(message.teamUuid, serverAccess.player(), BotTeamPermission.DELETE))
            return;

        Optional<BotTeam> team = BotTeamPersistentState.removeTeam(message.teamUuid);

        if (team.isEmpty())
            return;

        BotTeamsSyncS2CMessage.syncToPlayersUsingTeam(serverAccess.runtime(), team.get());
    }
}
