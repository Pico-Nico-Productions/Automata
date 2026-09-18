package pro.piconico.automata.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.registry.AutomataScreenHandlers;
import pro.piconico.automata.screen.slot.BotSlot;
import pro.piconico.automata.screen.slot.DisableableSlot;

public class RoboportScreenHandler extends BotDeviceScreenHandler {
    public static final int BOT_BAR_X = BODY_WIDTH / 2 - (RoboportBlockEntity.BOT_SLOT_COUNT * SLOT_DELTA - SLOT_SPACING) / 2,
            BOT_BAR_Y = BODY_HEIGHT / 2 - SLOT_DELTA - BAR_DELTA;
    public static final int INVENTORY_X = BODY_WIDTH / 2 - 4 * SLOT_DELTA - SLOT_SIZE / 2, INVENTORY_Y = BODY_HEIGHT / 2 - SLOT_DELTA;
    public static final int HOTBAR_Y = BODY_HEIGHT / 2 + SLOT_DELTA + BAR_DELTA;
    public static final int OFFSET_Y = BODY_INSET + TEXT_HEIGHT + UI_SPACING;

    private final Inventory inventory;

    public RoboportScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(AutomataScreenHandlers.ROBOPORT, syncId, ((BlockBotDevice)playerInventory.player.getEntityWorld().getBlockEntity(pos)));

        this.inventory = (RoboportBlockEntity)botDevice;

        for (int indexX = 0; indexX < RoboportBlockEntity.BOT_SLOT_COUNT; indexX++) {
            addSlot(new BotSlot(inventory, indexX, BOT_BAR_X + indexX * SLOT_DELTA, BOT_BAR_Y + OFFSET_Y));
        }

        for (int indexY = 0; indexY < 3; indexY++) {
            for (int indexX = 0; indexX < 9; indexX++) {
                addSlot(new DisableableSlot(playerInventory, indexY * 9 + indexX + 9, INVENTORY_X + indexX * SLOT_DELTA, INVENTORY_Y + indexY * SLOT_DELTA + OFFSET_Y));
            }
        }

        for (int indexX = 0; indexX < 9; indexX++) {
            addSlot(new DisableableSlot(playerInventory, indexX, INVENTORY_X + indexX * SLOT_DELTA, HOTBAR_Y + OFFSET_Y));
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

            if (slotIndex < RoboportBlockEntity.BOT_SLOT_COUNT) {
                if (!this.insertItem(originalStack, RoboportBlockEntity.BOT_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else {
                if (!this.insertItem(originalStack, 0, RoboportBlockEntity.BOT_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            }
            else {
                slot.markDirty();
            }
        }

        return newStack;
    }
}