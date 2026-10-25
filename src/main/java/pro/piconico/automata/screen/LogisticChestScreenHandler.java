package pro.piconico.automata.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.entity.LogisticChestBlockEntity;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.bot.device.BlockBotDevice.BlockId;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class LogisticChestScreenHandler extends BotDeviceScreenHandler {
    public LogisticChestScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(AutomataScreenHandlers.LOGISTIC_CHEST, syncId, BlockBotDevice.resolve(playerInventory.player, new BlockId(pos)).get());

        castLogisticChest().onOpen(playerInventory.player);

        addDeviceAndPlayerSlots(castLogisticChest(), playerInventory);
    }

    private LogisticChestBlockEntity castLogisticChest() {
        return (LogisticChestBlockEntity)device;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        castLogisticChest().onClose(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return castLogisticChest().canPlayerUse(player) && castLogisticChest().canPlayerOpenScreen(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ScreenHandlerUtils.quickMove(slots, this::insertItem, player, slot, castLogisticChest().size());
    }
}
