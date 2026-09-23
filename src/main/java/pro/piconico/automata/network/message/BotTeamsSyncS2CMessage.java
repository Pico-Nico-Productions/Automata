package pro.piconico.automata.network.message;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamsSyncS2CMessage(List<BotTeam> teams) {
    public BotTeamsSyncS2CMessage() {
        this(new ArrayList<>(BotTeamPersistentState.getTeams()));
    }

    public static boolean isUsingTeams(ServerPlayerEntity serverPlayer) {
        return serverPlayer.currentScreenHandler instanceof BotDeviceScreenHandler;
    }

    public static List<ServerPlayerEntity> getPlayersUsingTeams(MinecraftServer server) {
        List<ServerPlayerEntity> serverPlayers = server.getPlayerManager().getPlayerList();
        return serverPlayers.stream().filter(BotTeamsSyncS2CMessage::isUsingTeams).toList();
    }
}
