package pro.piconico.automata.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.network.message.BotTeamsSyncC2SMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.screen.slot.DisableableSlot;
import pro.piconico.automata.util.function.QuadFunction;
import pro.piconico.automata.world.BotTeamPersistentState;

public abstract class BotDeviceScreenHandler extends ScreenHandler {
    public static final int GENERIC_BODY_WIDTH = 176, GENERIC_BODY_HEIGHT = 168, INSET = 6, GAP = 4, DEVICE_INVENTORY_Y = 17, DEVICE_PLAYER_GAP = 14;

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

    public int getBodyWidth() {
        return GENERIC_BODY_WIDTH;
    }

    public int getBodyHeight() {
        return GENERIC_BODY_HEIGHT;
    }

    public int getInventoryX() {
        return (getBodyWidth() - 9 * ScreenHandlerUtils.SLOT_DELTA) / 2 + 1;
    }

    public Optional<BotTeam> getTeam() {
        Optional<UUID> teamUuid = device.getTeamUuid();

        if (teamUuid.isEmpty())
            return Optional.empty();

        return teams.stream().filter(t -> t.UUID.equals(teamUuid.get())).findAny();
    }

    protected void addDeviceAndPlayerSlots(Inventory deviceInventory, QuadFunction<Inventory, Integer, Integer, Integer, DisableableSlot> deviceSlotFactory,
            PlayerInventory playerInventory) {
        int deviceRowCount = ScreenHandlerUtils.getRowCount(deviceInventory.size());
        int inventoryX = getInventoryX();

        for (int yIndex = 0; yIndex < deviceRowCount; yIndex++) {
            for (int xIndex = 0; xIndex < 9; xIndex++) {
                int index = 9 * yIndex + xIndex;

                if (index >= deviceInventory.size())
                    break;

                int x = inventoryX + xIndex * ScreenHandlerUtils.SLOT_DELTA;
                int y = DEVICE_INVENTORY_Y + yIndex * ScreenHandlerUtils.SLOT_DELTA;
                addSlot(deviceSlotFactory.apply(deviceInventory, index, x, y));
            }
        }

        int playerInventoryY = DEVICE_INVENTORY_Y + deviceRowCount * ScreenHandlerUtils.SLOT_DELTA + DEVICE_PLAYER_GAP;
        for (int yIndex = 0; yIndex < 3; yIndex++) {
            for (int xIndex = 0; xIndex < 9; xIndex++) {
                int x = inventoryX + xIndex * ScreenHandlerUtils.SLOT_DELTA;
                int y = playerInventoryY + yIndex * ScreenHandlerUtils.SLOT_DELTA;
                addSlot(new DisableableSlot(playerInventory, yIndex * 9 + xIndex + 9, x, y));
            }
        }

        int hotbarY = playerInventoryY + 2 * ScreenHandlerUtils.SLOT_DELTA + ScreenHandlerUtils.BAR_DELTA;
        for (int xIndex = 0; xIndex < 9; xIndex++) {
            int x = inventoryX + xIndex * ScreenHandlerUtils.SLOT_DELTA;
            addSlot(new DisableableSlot(playerInventory, xIndex, x, hotbarY));
        }
    }

    protected void addDeviceAndPlayerSlots(Inventory deviceInventory, PlayerInventory playerInventory) {
        addDeviceAndPlayerSlots(deviceInventory, DisableableSlot::new, playerInventory);
    }
}
