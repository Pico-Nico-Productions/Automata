package pro.piconico.automata.network.message;

import java.util.List;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.world.BotTeamPersistentState;

public record BotTeamsSyncS2CMessage(List<BotTeam> teams) {
    public BotTeamsSyncS2CMessage(UUID playerUuid) {
        this(BotTeamPersistentState.getTeams().stream().filter(team -> team.hasMember(playerUuid)).toList());
    }

    public static boolean isUsingTeams(ServerPlayerEntity serverPlayer) {
        return serverPlayer.currentScreenHandler instanceof BotDeviceScreenHandler;
    }

    public static List<ServerPlayerEntity> getPlayersUsingTeams(MinecraftServer server) {
        List<ServerPlayerEntity> serverPlayers = server.getPlayerManager().getPlayerList();
        return serverPlayers.stream().filter(BotTeamsSyncS2CMessage::isUsingTeams).toList();
    }

    public static void syncToPlayersUsingTeam(MinecraftServer server, BotTeam team) {
        for (ServerPlayerEntity serverPlayer : getPlayersUsingTeams(server)) {
            if (!team.hasMember(serverPlayer.getUuid()))
                continue;

            AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(serverPlayer).send(new BotTeamsSyncS2CMessage(serverPlayer.getUuid()));
        }
    }
}
