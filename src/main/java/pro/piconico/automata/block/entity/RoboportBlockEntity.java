package pro.piconico.automata.block.entity;

import java.util.Optional;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.ConstructionBotEntity;
import pro.piconico.automata.registry.AutomataBlocks;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.screen.RoboportScreenHandler;

public class RoboportBlockEntity extends BlockEntity implements Inventory, ExtendedScreenHandlerFactory<BlockPos> {
    public static final int BOT_SLOT_COUNT = 1;

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(BOT_SLOT_COUNT, ItemStack.EMPTY);

    public RoboportBlockEntity(BlockPos pos, BlockState state) {
        super(AutomataEntities.ROBOPORT, pos, state);
    }

    public Optional<ConstructionBotEntity> assignJob(BotJob job) {
        if (getWorld().isClient())
            return Optional.empty();

        Optional<ItemStack> botStack = items.stream().filter(item -> item.isOf(AutomataItems.CONSTRUCTION_BOT)).findFirst();
        if (botStack.isEmpty()) {
            return Optional.empty();
        }

        botStack.get().decrement(1);

        ServerWorld serverWorld = (ServerWorld)getWorld();
        BlockPos spawnLocation = getPos().up();
        ConstructionBotEntity bot = AutomataEntities.CONSTRUCTION_BOT.spawn(serverWorld, spawnLocation, SpawnReason.MOB_SUMMONED);
        if (bot == null)
            return Optional.empty();

        bot.setJob(Optional.of(job));
        return Optional.of(bot);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, items);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, items);
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(itemStack -> itemStack.isEmpty());
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(items, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(items, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(AutomataBlocks.ROBOPORT.getTranslationKey());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RoboportScreenHandler(syncId, playerInventory, pos);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return pos;
    }
}
