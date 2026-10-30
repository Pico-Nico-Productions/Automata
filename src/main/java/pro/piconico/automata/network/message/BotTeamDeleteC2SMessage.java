package pro.piconico.automata.network.message;

import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamDeleteC2SMessage(UUID teamUuid) {
    public static void handle(BotTeamDeleteC2SMessage message, ServerAccess serverAccess) {
        if (!BotTeamPersistentState.teamExistsAndHasMemberWith(message.teamUuid, serverAccess.player(), BotTeamPermission.DELETE)) {
            BotTeamPersistentState.getTeam(message.teamUuid).ifPresent(team -> {
                serverAccess.player().sendMessage(AutomataTexts.getBotTeamPermissionMissing(serverAccess.player().getStringifiedName(), team), true);
            });
            return;
        }

        BotTeam team = BotTeamPersistentState.removeTeam(message.teamUuid).get();
        BotTeamsSyncS2CMessage.syncToPlayersUsingTeam(serverAccess.runtime(), team);
    }
}
