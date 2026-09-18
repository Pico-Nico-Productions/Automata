package pro.piconico.automata.network.message;

import java.util.List;
import pro.piconico.automata.bot.team.BotTeam;

public record BotTeamsSyncS2CMessage(List<BotTeam> teams) {
}
