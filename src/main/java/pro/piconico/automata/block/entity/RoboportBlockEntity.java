package pro.piconico.automata.block.entity;

import java.util.Optional;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
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
import pro.piconico.automata.bot.job.DeconstructionJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataBlocks;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.screen.RoboportScreenHandler;

public class RoboportBlockEntity extends BlockEntity implements Inventory, ExtendedScreenHandlerFactory<BlockPos> {
    private record BotItemAndEntity(Item item, EntityType<? extends BotEntity> entity) {
    }

    public static final int RANGE = 5;
    public static final int BOT_SLOT_COUNT = 1;

    private final DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(BOT_SLOT_COUNT, ItemStack.EMPTY);

    public RoboportBlockEntity(BlockPos pos, BlockState state) {
        super(AutomataEntities.ROBOPORT, pos, state);
    }

    public boolean isInRange(BlockPos blockPos) {
        return getPos().getChebyshevDistance(blockPos) <= RANGE;
    }

    private Optional<BotItemAndEntity> getBotFor(BotJob job) {
        if (!isInRange(job.pos()))
            return Optional.empty();

        Optional<BotItemAndEntity> bot;
        switch (job) {
        case DeconstructionJob deconstructionJob:
            bot = Optional.of(new BotItemAndEntity(AutomataItems.CONSTRUCTION_BOT, AutomataEntities.CONSTRUCTION_BOT));
            break;
        default:
            Automata.LOGGER
                    .warn(RoboportBlockEntity.class.getSimpleName() + " checked a job type it doesn't have a bot type for: " + job.getClass().getSimpleName());
            return Optional.empty();
        }

        return containsAny(itemStack -> itemStack.isOf(bot.get().item)) ? bot : Optional.empty();
    }

    public boolean canDoJob(BotJob job) {
        return getBotFor(job).isPresent();
    }

    public Optional<BotEntity> assignJob(BotJob job) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) {
            Automata.logError("Client can't assign jobs", IllegalCallerException::new);
            return Optional.empty();
        }
        
        Optional<BotItemAndEntity> bot = getBotFor(job);
        if (bot.isEmpty())
            return Optional.empty();

        Optional<ItemStack> botStack = itemStacks.stream().filter(item -> item.isOf(bot.get().item)).findFirst();
        botStack.get().decrement(1);
        markDirty();

        BlockPos spawnLocation = getPos().up();
        BotEntity botEntity = bot.get().entity.spawn(serverWorld, spawnLocation, SpawnReason.MOB_SUMMONED);
        botEntity.setJob(Optional.of(job));

        return Optional.of(botEntity);
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
