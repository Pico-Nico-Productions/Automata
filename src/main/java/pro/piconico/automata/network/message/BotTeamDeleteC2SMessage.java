package pro.piconico.automata.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamDeleteC2SMessage(UUID teamUuid) {
    public static void handle(BotTeamDeleteC2SMessage message, ServerAccess serverAccess) {
        Optional<BotTeam> team = BotTeamPersistentState.getTeam(message.teamUuid);

        if (team.isEmpty())
            return;

        if (!team.get().hasMemberWith(serverAccess.player(), BotTeamPermission.DELETE)) {
            serverAccess.player().sendMessage(AutomataTexts.getBotTeamPermissionMissing(serverAccess.player().getStringifiedName(), team.get()), true);
            return;
        }

        BotTeam deletedTeam = BotTeamPersistentState.removeTeam(message.teamUuid).get();
        BotTeamsSyncS2CMessage.syncToPlayersUsingTeam(serverAccess.runtime(), deletedTeam);
    }
}
