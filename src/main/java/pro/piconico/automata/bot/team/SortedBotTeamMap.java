package pro.piconico.automata.bot.team;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.mojang.serialization.Codec;

public class SortedBotTeamMap extends LinkedHashMap<UUID, BotTeam> {
    public static Codec<SortedBotTeamMap> CODEC = BotTeam.CODEC.listOf().xmap(SortedBotTeamMap::map, SortedBotTeamMap::flatten);

    public SortedBotTeamMap() {
        super();
    }

    private static List<BotTeam> flatten(SortedBotTeamMap teamMap) {
        return teamMap.values().stream().toList();
    }

    private static SortedBotTeamMap map(Collection<BotTeam> teams) {
        return (SortedBotTeamMap)teams.stream().collect(Collectors.toMap(team -> team.UUID, team -> team, (existing, replacement) -> existing, SortedBotTeamMap::new));
    }
}
