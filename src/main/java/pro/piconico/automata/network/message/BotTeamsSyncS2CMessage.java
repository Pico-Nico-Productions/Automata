package pro.piconico.automata.network.message;

import java.util.ArrayList;
import java.util.List;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamsSyncS2CMessage(List<BotTeam> teams) {
    public BotTeamsSyncS2CMessage() {
        this(new ArrayList<>(BotTeamPersistentState.getTeams()));
    }
}
