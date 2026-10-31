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
        Optional<BotTeam> team = BotTeamPersistentState.getTeam(message.teamUuid);

        if (team.isEmpty())
            return;

        if (!team.get().hasMemberWith(serverAccess.player(), BotTeamPermission.UPDATE)) {
            serverAccess.player().sendMessage(AutomataTexts.getBotTeamPermissionMissing(serverAccess.player().getStringifiedName(), team.get()), true);
            return;
        }

        Optional<BotTeam> updatedTeam = BotTeamPersistentState.updateTeam(message.teamUuid, message.settings);

        if (updatedTeam.isEmpty())
            return;

        BotTeamsSyncS2CMessage.syncToPlayersUsingTeam(serverAccess.runtime(), updatedTeam.get());
    }
}
