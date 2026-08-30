package pro.piconico.automata.block.entity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
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
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.item.BotItem;
import pro.piconico.automata.registry.AutomataBlocks;
import pro.piconico.automata.registry.AutomataBots;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataPointOfInterestTypes;
import pro.piconico.automata.screen.RoboportScreenHandler;
import pro.piconico.automata.util.math.ChunkUtils;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;
import pro.piconico.automata.world.BotTeamPersistentState;

public class RoboportBlockEntity extends BlockEntity implements Inventory, ExtendedScreenHandlerFactory<BlockPos> {
    private static final String TEAM_UUID_KEY = "team_uuid";
    public static final int CHUNK_RANGE = 0;
    public static final int BOT_SLOT_COUNT = 1;

    @FunctionalInterface
    public interface UpdateRoboport {
        void onUpdated(RoboportBlockEntity roboport);
    }

    public static final Event<UpdateRoboport> TEAM_CHANGED = EventFactory.createArrayBacked(UpdateRoboport.class, callbacks -> (roboport) -> {
        for (UpdateRoboport callback : callbacks) {
            callback.onUpdated(roboport);
        }
    });

    public static final Event<UpdateRoboport> BOT_ADDED = EventFactory.createArrayBacked(UpdateRoboport.class, callbacks -> (roboport) -> {
        for (UpdateRoboport callback : callbacks) {
            callback.onUpdated(roboport);
        }
    });

    private final DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(BOT_SLOT_COUNT, ItemStack.EMPTY);
    private Optional<UUID> teamUuid = Optional.empty();

    public RoboportBlockEntity(BlockPos pos, BlockState state) {
        super(AutomataEntities.ROBOPORT, pos, state);
        // TODO: Replace with team select screen
        teamUuid = Optional.of(BotTeamPersistentState.getTeamMap().firstEntry().getKey());
    }

    private Optional<BotEntity> getBotEntityFor(BotJob job) {
        Optional<Set<BotType>> capableBotTypes = AutomataBots.getBotTypesFor(job);
        if (capableBotTypes.isEmpty())
            return Optional.empty();

        Box searchBox = ChunkBounds.of(new ChunkPos(getPos()), CHUNK_RANGE, world).toBox();
        List<BotEntity> capableBots = world.getEntitiesByType(TypeFilter.instanceOf(BotEntity.class), searchBox,
                botEntity -> botEntity.isAlive() && botEntity.getTeamUuid().equals(teamUuid.get()) && !botEntity.hasJob() && botEntity.canDoJob(job));
        return capableBots.isEmpty() ? Optional.empty() : Optional.of(capableBots.getFirst());
    }

    private Optional<Integer> getBotSlotFor(BotJob job) {
        Optional<Set<BotType>> capableBotTypes = AutomataBots.getBotTypesFor(job);
        if (capableBotTypes.isEmpty())
            return Optional.empty();

        Set<Item> capableBotItems = capableBotTypes.get().stream().map(botType -> botType.item()).collect(Collectors.toSet());
        for (int i = 0; i < itemStacks.size(); i++) {
            if (capableBotItems.contains(getStack(i).getItem()))
                return Optional.of(i);
        }

        return Optional.empty();
    }

    public boolean canAssignJob(BotJob job) {
        return teamUuid.isPresent() && (getBotEntityFor(job).isPresent() || getBotSlotFor(job).isPresent());
    }

    public void setTeam(Optional<UUID> teamUuid) {
        this.teamUuid = teamUuid;
        TEAM_CHANGED.invoker().onUpdated(this);
    }

    public Optional<BotEntity> assignJob(BotJob job) {
        if (teamUuid.isEmpty())
            return Optional.empty();

        Optional<BotEntity> botEntity = getBotEntityFor(job);
        if (botEntity.isPresent() && botEntity.get().setJob(Optional.of(job)))
            return botEntity;

        Optional<Integer> botSlot = getBotSlotFor(job);
        if (botSlot.isEmpty())
            return Optional.empty();

        BotItem botItem = (BotItem)Inventories.splitStack(itemStacks, botSlot.get(), 1).getItem();
        markDirty();

        EntityType<? extends BotEntity> botEntityType = botItem.getBotType().entityType();
        BotEntity newBotEntity = BotEntity.spawn(botEntityType, (ServerWorld)getWorld(), getPos().up(), teamUuid.get());
        newBotEntity.setJob(Optional.of(job));

        return Optional.of(newBotEntity);
    }

    public boolean tryAdd(BotEntity botEntity) {
        Item botItem = botEntity.getBotType().item();

        if (!InventoryUtils.canAdd(this, botItem))
            return false;

        botEntity.stopRiding();
        botEntity.removeAllPassengers();
        botEntity.detachLeash();
        botEntity.discard();

        InventoryUtils.add(this, botItem);
        markDirty();

        BOT_ADDED.invoker().onUpdated(this);

        return true;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, itemStacks);
        teamUuid = view.read(TEAM_UUID_KEY, Uuids.INT_STREAM_CODEC);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, itemStacks);
        if (teamUuid.isEmpty())
            return;

        view.put(TEAM_UUID_KEY, Uuids.INT_STREAM_CODEC, teamUuid.get());
    }

    @Override
    public void clear() {
        itemStacks.clear();
        markDirty();
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
        ItemStack splitStack = Inventories.splitStack(itemStacks, slot, amount);
        markDirty();

        return splitStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removedStack = Inventories.removeStack(itemStacks, slot);
        markDirty();

        return removedStack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack currentStack = getStack(slot);
        boolean added = !stack.isEmpty() && stack.getItem() != currentStack.getItem() || stack.getCount() > currentStack.getCount();

        itemStacks.set(slot, stack);
        markDirty();

        if (added) {
            BOT_ADDED.invoker().onUpdated(this);
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack.getItem() instanceof BotItem;
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

    public static Optional<RoboportBlockEntity> getClosestTo(BlockPos pos, int chunkRange, Predicate<RoboportBlockEntity> predicate, ServerWorld serverWorld) {
        Optional<BlockPos> closestRoboportPos = serverWorld.getPointOfInterestStorage()
                .getInSquare(entry -> entry.matchesKey(AutomataPointOfInterestTypes.ROBOPORT), pos, ChunkUtils.CHUNK_SIZE * chunkRange,
                        PointOfInterestStorage.OccupationStatus.ANY)
                .map(PointOfInterest::getPos)
                .filter(roboportPos -> serverWorld.getBlockEntity(roboportPos) instanceof RoboportBlockEntity roboport && predicate.test(roboport))
                .min(Comparator.comparingDouble(roboport -> pos.getSquaredDistance(roboport)));

        if (closestRoboportPos.isEmpty())
            return Optional.empty();

        return Optional.of((RoboportBlockEntity)serverWorld.getBlockEntity(closestRoboportPos.get()));
    }
}
