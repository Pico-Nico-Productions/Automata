package pro.piconico.automata.screen;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.registry.AutomataScreenHandlers;
import pro.piconico.automata.screen.slot.BotSlot;

public class RoboportScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public RoboportScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(AutomataScreenHandlers.ROBOPORT, syncId);

        World world = playerInventory.player.getEntityWorld();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof RoboportBlockEntity roboport)) {
            throw new IllegalStateException("Incorrect block entity at " + pos);
        }

        this.inventory = roboport;

        // Bot slot(s)
        for (int i = 0; i < RoboportBlockEntity.BOT_SLOT_COUNT; i++) {
            addSlot(new BotSlot(inventory, i, 80, 34));
        }

        // Player inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        // Hotbar
        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            // Roboport slot -> Player inventory
            if (slotIndex < RoboportBlockEntity.BOT_SLOT_COUNT) {
                if (!this.insertItem(originalStack, RoboportBlockEntity.BOT_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Player inventory -> Roboport slot
            else {
                if (!this.insertItem(originalStack, 0, RoboportBlockEntity.BOT_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }
}