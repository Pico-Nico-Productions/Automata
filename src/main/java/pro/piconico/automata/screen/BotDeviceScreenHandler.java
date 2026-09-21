package pro.piconico.automata.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.network.message.BotTeamsSyncC2SMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.world.BotTeamPersistentState;

public abstract class BotDeviceScreenHandler extends ScreenHandler {
    public static final int BODY_WIDTH = 176, BODY_HEIGHT = 125, UI_SPACING = 4, BODY_INSET = 7, TEXT_HEIGHT = 9;

    public final BotDevice<?> device;
    public final List<BotTeam> teams;

    protected BotDeviceScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, BotDevice<?> device) {
        super(type, syncId);
        this.device = device;
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            teams = new ArrayList<>();

            AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotTeamsSyncC2SMessage());
        }
        else {
            teams = new ArrayList<>(BotTeamPersistentState.getTeams());
        }
    }

    public Optional<BotTeam> getTeam() {
        Optional<UUID> teamUuid = device.getTeamUuid();

        if (teamUuid.isEmpty())
            return Optional.empty();

        return teams.stream().filter(t -> t.UUID.equals(teamUuid.get())).findAny();
    }
}
