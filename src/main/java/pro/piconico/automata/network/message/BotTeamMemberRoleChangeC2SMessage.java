package pro.piconico.automata.network.message;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamRole;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamMemberRoleChangeC2SMessage(UUID teamUuid, UUID targetPlayerUuid, Optional<BotTeamRole> role) {
    public BotTeamMemberRoleChangeC2SMessage(UUID teamUuid, UUID targetPlayerUuid, BotTeamRole role) {
        this(teamUuid, targetPlayerUuid, Optional.of(role));
    }

    public BotTeamMemberRoleChangeC2SMessage(UUID teamUuid, UUID targetPlayerUuid) {
        this(teamUuid, targetPlayerUuid, Optional.empty());
    }

    public static boolean canChangeRole(PlayerEntity senderPlayer, BotTeam team, UUID targetPlayerUuid) {
        if (senderPlayer.getUuid().equals(targetPlayerUuid))
            return false;

        if (!team.hasMemberWith(senderPlayer, BotTeamPermission.CHANGE_MEMBER_ROLE))
            return false;

        if (team.getRole(senderPlayer).get().ordinal() < team.getRole(targetPlayerUuid).map(role -> role.ordinal()).orElse(0))
            return false;

        return true;
    }

    public static void handle(BotTeamMemberRoleChangeC2SMessage message, ServerAccess serverAccess) {
        Optional<BotTeam> team = BotTeamPersistentState.getTeam(message.teamUuid);

        if (team.isEmpty())
            return;

        if (!canChangeRole(serverAccess.player(), team.get(), message.targetPlayerUuid)) {
            serverAccess.player().sendMessage(AutomataTexts.getBotTeamPermissionMissing(serverAccess.player().getStringifiedName(), team.get()), true);
            return;
        }

        Optional<BotTeamRole> role = team.get().getRole(message.targetPlayerUuid);

        if (role.equals(message.role))
            return;

        if (message.role.isPresent()) {
            if (role.isPresent()) {
                team.get().setRole(message.targetPlayerUuid, message.role.get());
            }
            else {
                ServerPlayerEntity serverPlayer = serverAccess.runtime().getPlayerManager().getPlayer(message.targetPlayerUuid);

                if (serverPlayer == null)
                    return;

                team.get().addMember(serverPlayer, message.role.get());
            }
        }
        else {
            team.get().removeMember(message.targetPlayerUuid);
        }

        BotTeamsSyncS2CMessage.syncToPlayersUsingTeam(serverAccess.runtime(), team.get());
    }
}
