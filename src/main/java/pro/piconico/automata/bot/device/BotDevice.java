package pro.piconico.automata.bot.device;

import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;

public interface BotDevice<T> extends ExtendedScreenHandlerFactory<T> {
    @FunctionalInterface
    public interface ChangeServerBotTeam {
        void onChanged(ServerWorld serverWorld, BotDevice<?> device, Optional<UUID> oldTeamUuid);
    }

    public static final Event<ChangeServerBotTeam> SERVER_TEAM_CHANGED = EventFactory.createArrayBacked(ChangeServerBotTeam.class,
            callbacks -> (serverWorld, device, oldTeamUuid) -> {
                for (ChangeServerBotTeam callback : callbacks) {
                    callback.onChanged(serverWorld, device, oldTeamUuid);
                }
            });

    @FunctionalInterface
    public interface ChangeBotTeam {
        void onChanged(Optional<UUID> oldTeamUuid);
    }

    void addTeamChangedListener(ChangeBotTeam listener);

    void removeTeamChangedListener(ChangeBotTeam listener);

    Optional<UUID> getTeamUuid();

    boolean setTeamUuid(Optional<UUID> teamUuid);
}
