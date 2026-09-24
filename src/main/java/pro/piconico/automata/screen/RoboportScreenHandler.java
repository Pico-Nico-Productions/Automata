package pro.piconico.automata.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.registry.AutomataScreenHandlers;
import pro.piconico.automata.screen.slot.BotSlot;

public class RoboportScreenHandler extends BotDeviceScreenHandler {
    private final Inventory inventory;

    public RoboportScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(AutomataScreenHandlers.ROBOPORT, syncId, ((BlockBotDevice)playerInventory.player.getEntityWorld().getBlockEntity(pos)));

        inventory = (RoboportBlockEntity)device;

        addDeviceAndPlayerSlots(inventory, BotSlot::new, playerInventory);
    }

    @Override
    public int getBodyHeight() {
        return GENERIC_BODY_HEIGHT - (3 - ScreenHandlerUtils.getRowCount(inventory.size())) * ScreenHandlerUtils.SLOT_DELTA;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        return ScreenHandlerUtils.quickMove(slots, this::insertItem, player, slotIndex, inventory.size());
    }
}