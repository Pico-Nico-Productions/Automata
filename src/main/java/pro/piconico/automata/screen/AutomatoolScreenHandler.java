package pro.piconico.automata.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class AutomatoolScreenHandler extends ScreenHandler {
    public AutomatoolScreenHandler(int syncId, PlayerInventory playerInventory, ItemStack stack) {
        super(AutomataScreenHandlers.AUTOMATOOL, syncId);
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
