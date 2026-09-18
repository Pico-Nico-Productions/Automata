package pro.piconico.automata.screen;

import java.util.ArrayList;
import java.util.List;
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
    public static final int SLOT_SIZE = 16, SLOT_SPACING = 2, BAR_SPACING = 6;
    public static final int SLOT_DELTA = SLOT_SIZE + SLOT_SPACING, BAR_DELTA = SLOT_SIZE + BAR_SPACING;

    public final BotDevice<?> botDevice;
    public final List<BotTeam> teams;

    protected BotDeviceScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, BotDevice<?> botDevice) {
        super(type, syncId);
        this.botDevice = botDevice;
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            teams = new ArrayList<>();

            AutomataMessages.CHANNEL.clientHandle().send(new BotTeamsSyncC2SMessage());
        }
        else {
            teams = new ArrayList<>(BotTeamPersistentState.getTeams());
        }
    }
}
