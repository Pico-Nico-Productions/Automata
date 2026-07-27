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
import pro.piconico.automata.Automata;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.ConstructionBotEntity;
import pro.piconico.automata.registry.AutomataBlocks;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.screen.RoboportScreenHandler;

public class RoboportBlockEntity extends BlockEntity implements Inventory, ExtendedScreenHandlerFactory<BlockPos> {
    private static final int RANGE = 2;
    
    public static final int BOT_SLOT_COUNT = 1;

    private final DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(BOT_SLOT_COUNT, ItemStack.EMPTY);

    public RoboportBlockEntity(BlockPos pos, BlockState state) {
        super(AutomataEntities.ROBOPORT, pos, state);
    }

    public boolean isInRange(BlockPos blockPos) {
        return getPos().getChebyshevDistance(blockPos) <= RANGE;
    }

    public boolean canDoJob(BotJob job) {
        if (!isInRange(job.pos()))
            return false;

        Optional<ItemStack> botStack = itemStacks.stream().filter(item -> item.isOf(AutomataItems.CONSTRUCTION_BOT)).findFirst();
        return botStack.isPresent();
    }

    public Optional<ConstructionBotEntity> assignJob(BotJob job) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) {
            Automata.logError("Client can't assign jobs", IllegalCallerException::new);
            return Optional.empty();
        }

        if (!canDoJob(job))
            return Optional.empty();

        Optional<ItemStack> botStack = itemStacks.stream().filter(item -> item.isOf(AutomataItems.CONSTRUCTION_BOT)).findFirst();
        botStack.get().decrement(1);

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
        Inventories.readData(view, itemStacks);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, itemStacks);
    }

    @Override
    public void clear() {
        itemStacks.clear();
    }

    @Override
    public int size() {
        return itemStacks.size();
    }

    @Override
    public boolean isEmpty() {
        return itemStacks.stream().allMatch(itemStack -> itemStack.isEmpty());
    }

    @Override
    public ItemStack getStack(int slot) {
        return itemStacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(itemStacks, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(itemStacks, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        itemStacks.set(slot, stack);
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
