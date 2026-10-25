package pro.piconico.automata.screen;

import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import pro.piconico.automata.bot.device.EntityBotDevice;
import pro.piconico.automata.bot.device.EntityBotDevice.EntityId;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class BotScreenHandler extends BotDeviceScreenHandler {
    public BotScreenHandler(int syncId, PlayerInventory playerInventory, UUID entityUuid) {
        super(AutomataScreenHandlers.BOT, syncId, EntityBotDevice.resolve(playerInventory.player, new EntityId(entityUuid)).get());

        addDeviceAndPlayerSlots(castBot(), playerInventory);
    }

    private BotEntity castBot() {
        return (BotEntity)device;
    }

    @Override
    public int getBodyHeight() {
        return calculateBodyHeight(ScreenHandlerUtils.getRowCount(castBot().size()));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return castBot().canPlayerUse(player) && castBot().canPlayerOpenScreen(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ScreenHandlerUtils.quickMove(slots, this::insertItem, player, slot, castBot().size());
    }
}
