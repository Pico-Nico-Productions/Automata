package pro.piconico.automata.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeam.BotTeamSettings;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamUpdateC2SMessage(UUID teamUuid, BotTeamSettings settings) {
    public static void handle(BotTeamUpdateC2SMessage message, ServerAccess serverAccess) {
        if (!BotTeamPersistentState.teamExistsAndHasMemberWith(message.teamUuid, serverAccess.player(), BotTeamPermission.UPDATE)) {
            BotTeamPersistentState.getTeam(message.teamUuid).ifPresent(team -> {
                serverAccess.player().sendMessage(AutomataTexts.getTeamPermissionMissing(serverAccess.player().getStringifiedName(), team), true);
            });
            return;
        }

        Optional<BotTeam> team = BotTeamPersistentState.updateTeam(message.teamUuid, message.settings);

        if (team.isEmpty())
            return;

        BotTeamsSyncS2CMessage.syncToPlayersUsingTeam(serverAccess.runtime(), team.get());
    }
}
