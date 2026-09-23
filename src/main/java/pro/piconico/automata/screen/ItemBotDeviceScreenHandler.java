package pro.piconico.automata.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class ItemBotDeviceScreenHandler extends BotDeviceScreenHandler {
    public ItemBotDeviceScreenHandler(int syncId, PlayerInventory playerInventory, ItemStack automatoolStack) {
        super(AutomataScreenHandlers.ITEM_BOT_DEVICE, syncId, new ItemBotDevice(playerInventory.player, automatoolStack));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }
}
