package pro.piconico.automata.bot.device;

import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;

public interface BotDevice<T> extends ExtendedScreenHandlerFactory<T> {
    @FunctionalInterface
    public interface ChangeTeam {
        void onChanged(BotDevice<?> botDevice, Optional<UUID> oldTeamUuid);
    }

    public static final Event<ChangeTeam> TEAM_CHANGED = EventFactory.createArrayBacked(ChangeTeam.class, callbacks -> (botDevice, oldTeamUuid) -> {
        for (ChangeTeam callback : callbacks) {
            callback.onChanged(botDevice, oldTeamUuid);
        }
    });

    Optional<UUID> getTeamUuid();

    boolean setTeamUuid(Optional<UUID> teamUuid);
}
