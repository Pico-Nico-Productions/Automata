package pro.piconico.automata.network.message;

import java.util.Optional;
import io.wispforest.owo.network.ServerAccess;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamCreateC2SMessage(BotTeam team) {
    public static void handle(BotTeamCreateC2SMessage message, ServerAccess serverAccess) {
        Optional<BotTeam> team = BotTeamPersistentState.addTeam(message.team);

        if (team.isEmpty())
            return;

        AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(serverAccess.player()).send(new BotTeamsSyncS2CMessage());
    }
}
