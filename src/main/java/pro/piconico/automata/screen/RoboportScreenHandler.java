package pro.piconico.automata.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.bot.device.BlockBotDevice.BlockId;
import pro.piconico.automata.registry.AutomataScreenHandlers;
import pro.piconico.automata.screen.slot.BotSlot;

public class RoboportScreenHandler extends BotDeviceScreenHandler {
    public RoboportScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(AutomataScreenHandlers.ROBOPORT, syncId, BlockBotDevice.resolve(playerInventory.player, new BlockId(pos)).get());

        addDeviceAndPlayerSlots(castRoboport(), BotSlot::new, playerInventory);
    }

    private RoboportBlockEntity castRoboport() {
        return (RoboportBlockEntity)device;
    }

    @Override
    public int getBodyHeight() {
        return calculateBodyHeight(ScreenHandlerUtils.getRowCount((castRoboport()).size()));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ScreenHandlerUtils.quickMove(slots, this::insertItem, player, slot, (castRoboport()).size());
    }
}