package pro.piconico.automata.block.entity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.inventory.SimpleInventory;
import pro.piconico.automata.item.BotItem;
import pro.piconico.automata.registry.AutomataBots;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataPointOfInterestTypes;
import pro.piconico.automata.screen.RoboportScreenHandler;
import pro.piconico.automata.util.math.ChunkUtils;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

public class RoboportBlockEntity extends BlockBotDevice implements SimpleInventory {
    public static final int CHUNK_RANGE = 0;
    public static final int BOT_SLOT_COUNT = 3;

    @FunctionalInterface
    public interface UpdateRoboport {
        void onUpdated(RoboportBlockEntity roboport);
    }

    public static final Event<UpdateRoboport> BOT_ADDED = EventFactory.createArrayBacked(UpdateRoboport.class, callbacks -> (roboport) -> {
        for (UpdateRoboport callback : callbacks) {
            callback.onUpdated(roboport);
        }
    });

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(BOT_SLOT_COUNT, ItemStack.EMPTY);

    public RoboportBlockEntity(BlockPos pos, BlockState state) {
        super(AutomataEntities.ROBOPORT, pos, state);
    }

    //#region Bot
    private Optional<BotEntity> getBotEntityFor(BotJob job) {
        Set<BotType> capableBotTypes = AutomataBots.getBotTypesFor(job);
        if (capableBotTypes.isEmpty())
            return Optional.empty();

        Box searchBox = ChunkBounds.of(new ChunkPos(getPos()), CHUNK_RANGE, world).toBox();
        List<BotEntity> capableBots = world.getEntitiesByType(TypeFilter.instanceOf(BotEntity.class), searchBox,
                botEntity -> botEntity.isAlive() && botEntity.getTeamUuid().equals(getTeamUuid()) && botEntity.canDoJob(job));
        return capableBots.isEmpty() ? Optional.empty() : Optional.of(capableBots.getFirst());
    }

    private Optional<Integer> getBotSlotFor(BotJob job) {
        Set<BotType> capableBotTypes = AutomataBots.getBotTypesFor(job);

        if (capableBotTypes.isEmpty())
            return Optional.empty();

        Set<Item> capableBotItems = capableBotTypes.stream().map(botType -> botType.item()).collect(Collectors.toSet());
        for (int i = 0; i < inventory.size(); i++) {
            if (capableBotItems.contains(getStack(i).getItem()))
                return Optional.of(i);
        }

        return Optional.empty();
    }

    public boolean canDoJob(BotJob job) {
        return getTeamUuid().isPresent() && (getBotEntityFor(job).isPresent() || getBotSlotFor(job).isPresent());
    }

    public Optional<BotEntity> getOrSpawnBotFor(BotJob job) {
        if (getTeamUuid().isEmpty())
            return Optional.empty();

        Optional<BotEntity> botEntity = getBotEntityFor(job);
        if (botEntity.isPresent())
            return botEntity;

        Optional<Integer> botSlot = getBotSlotFor(job);
        if (botSlot.isEmpty())
            return Optional.empty();

        BotItem botItem = (BotItem)Inventories.splitStack(inventory, botSlot.get(), 1).getItem();
        markDirty();

        EntityType<? extends BotEntity> botEntityType = botItem.getBotType().entityType();
        BotEntity newBotEntity = botEntityType.spawn((ServerWorld)getWorld(), getPos().up(), SpawnReason.MOB_SUMMONED);
        newBotEntity.setTeamUuid(getTeamUuid());

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
    //#endregion

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
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RoboportScreenHandler(syncId, playerInventory, pos);
    }
    //#endregion

    //#region SimpleInventory
    @Override
    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack currentStack = getStack(slot);
        boolean added = !stack.isEmpty() && stack.getItem() != currentStack.getItem() || stack.getCount() > currentStack.getCount();

        inventory.set(slot, stack);
        markDirty();

        if (added) {
            BOT_ADDED.invoker().onUpdated(this);
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack.getItem() instanceof BotItem;
    }
    //#endregion

    public static Optional<RoboportBlockEntity> getClosestTo(BlockPos pos, Predicate<RoboportBlockEntity> predicate, ServerWorld serverWorld) {
        Optional<BlockPos> closestRoboportPos = serverWorld.getPointOfInterestStorage()
                .getInSquare(entry -> entry.matchesKey(AutomataPointOfInterestTypes.ROBOPORT), pos, ChunkUtils.CHUNK_SIZE * (CHUNK_RANGE + 1),
                        PointOfInterestStorage.OccupationStatus.ANY)
                .map(PointOfInterest::getPos).filter(roboportPos -> serverWorld.getBlockEntity(roboportPos, AutomataEntities.ROBOPORT)
                        .filter(roboport -> predicate.test(roboport)).isPresent())
                .min(Comparator.comparingDouble(roboport -> pos.getSquaredDistance(roboport)));

        if (closestRoboportPos.isEmpty())
            return Optional.empty();

        return serverWorld.getBlockEntity(closestRoboportPos.get(), AutomataEntities.ROBOPORT);
    }
}
