package pro.piconico.automata.network.message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeam.BotTeamSettings;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamCreateC2SMessage(BotTeamSettings settings, UUID ownerUuid) {
    public static void handle(BotTeamCreateC2SMessage message, ServerAccess serverAccess) {
        Optional<BotTeam> team = BotTeamPersistentState.addTeam(message.settings, message.ownerUuid);

        if (team.isEmpty())
            return;

        List<ServerPlayerEntity> playersUsingTeams = BotTeamsSyncS2CMessage.getPlayersUsingTeams(serverAccess.runtime());
        AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(playersUsingTeams).send(new BotTeamsSyncS2CMessage());
    }
}
