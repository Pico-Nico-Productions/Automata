package pro.piconico.automata.bot.device;

import net.minecraft.inventory.ListInventory;
import net.minecraft.util.math.BlockPos;

public interface LogisticStorage<T> extends BotDevice<T>, ListInventory {
    public BlockPos getPos();
}
