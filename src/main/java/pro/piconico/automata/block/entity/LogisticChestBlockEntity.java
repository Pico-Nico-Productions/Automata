package pro.piconico.automata.block.entity;

import org.jspecify.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestLidAnimator;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.bot.device.LogisticStorage;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.screen.LogisticChestScreenHandler;

public class LogisticChestBlockEntity extends BlockBotDevice implements LogisticStorage<BlockPos>, LidOpenable {
    public static final int INVENTORY_SIZE = 27;
    private static final int VIEWER_COUNT_UPDATE_EVENT_TYPE = 1;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final ChestLidAnimator lidAnimator = new ChestLidAnimator();
    private final ViewerCountManager viewerCountManager = new ViewerCountManager() {
        @Override
        protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
        }

        @Override
        protected void onContainerClose(World world, BlockPos pos, BlockState state) {
        }

        @Override
        protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
            world.addSyncedBlockEvent(pos, state.getBlock(), VIEWER_COUNT_UPDATE_EVENT_TYPE, newViewerCount);
        }

        @Override
        public boolean isPlayerViewing(PlayerEntity player) {
            return player.isViewingContainerAt(this, pos);
        }
    };

    public LogisticChestBlockEntity(BlockPos pos, BlockState state) {
        super(AutomataEntities.LOGISTIC_CHEST, pos, state);
    }

    public void tickClient(World world, BlockPos pos, BlockState state) {
        lidAnimator.step();
    }

    public void onOpen(PlayerEntity player) {
        if (!removed) {
            viewerCountManager.openContainer(player, getWorld(), getPos(), getCachedState(), player.getBlockInteractionRange());
        }
    }

    public void onClose(PlayerEntity player) {
        if (!removed) {
            viewerCountManager.closeContainer(player, getWorld(), getPos(), getCachedState());
        }
    }

    //#region BlockBotDevice
    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, inventory);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, inventory);
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new LogisticChestScreenHandler(syncId, playerInventory, pos);
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type != VIEWER_COUNT_UPDATE_EVENT_TYPE)
            return super.onSyncedBlockEvent(type, data);

        lidAnimator.setOpen(data > 0);

        return true;
    }
    //#endregion

    //#region SimpleInventory
    @Override
    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }
    //#endregion

    //#region LidOpenable
    @Override
    public float getAnimationProgress(float tickProgress) {
        return lidAnimator.getProgress(tickProgress);
    }
    //#endregion
}
