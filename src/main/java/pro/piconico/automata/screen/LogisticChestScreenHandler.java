package pro.piconico.automata.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.entity.LogisticChestBlockEntity;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class LogisticChestScreenHandler extends BotDeviceScreenHandler {
    private final LogisticChestBlockEntity logisticChest;

    public LogisticChestScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(AutomataScreenHandlers.LOGISTIC_CHEST, syncId, ((BlockBotDevice)playerInventory.player.getEntityWorld().getBlockEntity(pos)));

        logisticChest = (LogisticChestBlockEntity)device;
        logisticChest.onOpen(playerInventory.player);

        addDeviceAndPlayerSlots(logisticChest, playerInventory);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        logisticChest.onClose(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return logisticChest.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ScreenHandlerUtils.quickMove(slots, this::insertItem, player, slot, logisticChest.size());
    }
}
