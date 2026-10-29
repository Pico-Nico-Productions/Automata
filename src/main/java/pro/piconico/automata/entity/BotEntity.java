package pro.piconico.automata.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.ListInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.device.EntityBotDevice;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.entity.ai.goal.BotDoJobGoal;
import pro.piconico.automata.entity.ai.goal.BotGrabJobItemsGoal;
import pro.piconico.automata.entity.ai.goal.BotHoverGoal;
import pro.piconico.automata.entity.ai.goal.BotPickUpItemsGoal;
import pro.piconico.automata.entity.ai.goal.BotStoreItemsGoal;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.item.ItemUtils.PredicateItemStack;
import pro.piconico.automata.screen.BotScreenHandler;
import pro.piconico.automata.util.math.ChunkUtils;
import pro.piconico.automata.entity.ai.goal.BotReturnToRoboportGoal;
import pro.piconico.automata.world.BotJobPersistentState;

public abstract class BotEntity extends EntityBotDevice implements ListInventory {
    private static final String ITEMS_TO_PICK_UP_KEY = "items_to_pick_up";
    public static final double MAX_HEALTH = 10;
    public static final int INVENTORY_SIZE = 3;
    public static final double SPEED = 0.65;
    public static final int INTERACT_DISTANCE = 1;
    public static final float MAX_FOLLOW_RANGE = 2F * ChunkUtils.CHUNK_SIZE;

    @FunctionalInterface
    public interface EndJob {
        void onEnded(BotEntity bot, BotJob job, boolean completed);
    }

    public static final Event<EndJob> JOB_ENDED = EventFactory.createArrayBacked(EndJob.class, callbacks -> (bot, job, completed) -> {
        for (EndJob callback : callbacks) {
            callback.onEnded(bot, job, completed);
        }
    });

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final Set<UUID> itemsToPickUp = new HashSet<>();

    public BotEntity(EntityType<? extends BotEntity> entityType, World world) {
        super(entityType, world);
        moveControl = new FlightMoveControl(this, getMaxLookPitchChange(), true);
    }

    public static DefaultAttributeContainer.Builder createBotAttributes() {
        return PathAwareEntity.createMobAttributes() //
                .add(EntityAttributes.MAX_HEALTH, MAX_HEALTH) //
                .add(EntityAttributes.MOVEMENT_SPEED, SPEED) //
                .add(EntityAttributes.FLYING_SPEED, SPEED);
    }

    public abstract BotType getBotType();

    @Override
    public boolean setTeamUuid(Optional<UUID> teamUuid) {
        if (!super.setTeamUuid(teamUuid))
            return false;

        if (getEntityWorld() instanceof ServerWorld) {
            endJob(false);
        }

        return true;
    }

    public Optional<BotJob> getJob() {
        return BotJobPersistentState.getJob((ServerWorld)getEntityWorld(), uuid);
    }

    public static boolean couldReach(EntityType<? extends BotEntity> type, World world, BlockPos startPos, BlockPos targetPos) {
        BotEntity bot = type.create(world, SpawnReason.MOB_SUMMONED);

        if (bot == null) {
            return false;
        }

        bot.refreshPositionAndAngles(startPos.getX() + .5, startPos.getY() + .5, startPos.getZ() + .5, 0F, 0F);
        Path path = bot.getNavigation().findPathTo(targetPos, INTERACT_DISTANCE);
        return path != null && path.reachesTarget();
    }

    public boolean canDoJob(BotJob job) {
        if (getTeamUuid().isEmpty() || getJob().isPresent() || !getBotType().supportedJobTypes().contains(job.getType()))
            return false;

        List<PredicateItemStack> missingStacks = InventoryUtils.getStacks(this, job.getRequiredStacks(getEntityWorld()), false);
        boolean hasStacks = BotNetworkManager.hasStacks((ServerWorld)getEntityWorld(), getTeamUuid().get(), getBlockPos(), missingStacks);
        if (!hasStacks)
            return false;

        Path pathToJob = getNavigation().findPathTo(job.pos(), INTERACT_DISTANCE);
        return pathToJob != null && pathToJob.reachesTarget();
    }

    public void endJob(boolean completed) {
        Optional<BotJob> job = getJob();

        if (job.isEmpty())
            return;

        JOB_ENDED.invoker().onEnded(this, job.get(), completed);
    }

    public boolean hasEmptyStack() {
        return getFilledSlotCount() < size();
    }

    public Set<ItemEntity> getItemsToPickUp() {
        World world = getEntityWorld();
        return itemsToPickUp.stream().map(uuid -> (ItemEntity)world.getEntity(uuid)).filter(entity -> entity != null).collect(Collectors.toSet());
    }

    public void addItemsToPickUp(Collection<ItemEntity> items) {
        itemsToPickUp.addAll(items.stream().map(entity -> entity.getUuid()).toList());
    }

    public void removeInvalidItemsToPickUp() {
        World world = getEntityWorld();
        itemsToPickUp.removeIf(uuid -> !(world.getEntity(uuid) instanceof ItemEntity entity) || entity.isRemoved());
    }

    //#region EntityBotDevice
    @Override
    protected void initGoals() {
        goalSelector.add(0, new BotGrabJobItemsGoal(this, SPEED, INTERACT_DISTANCE));
        goalSelector.add(1, new BotPickUpItemsGoal(this, this, SPEED, INTERACT_DISTANCE));
        goalSelector.add(2, new BotDoJobGoal(this, SPEED, INTERACT_DISTANCE));
        goalSelector.add(3, new BotStoreItemsGoal(this, SPEED, INTERACT_DISTANCE));
        goalSelector.add(4, new BotReturnToRoboportGoal(this, SPEED, INTERACT_DISTANCE));
        goalSelector.add(5, new BotHoverGoal(this));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world) {
            public boolean isValidPosition(BlockPos pos) {
                return world.getBlockState(pos).isAir();
            }
        };
        birdNavigation.setCanOpenDoors(false);
        birdNavigation.setCanSwim(false);
        birdNavigation.setMaxFollowRange(MAX_FOLLOW_RANGE);
        return birdNavigation;
    }

    @Override
    public BlockPos getPos() {
        return getBlockPos();
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BotScreenHandler(syncId, playerInventory, uuid);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public @Nullable Entity teleportTo(TeleportTarget teleportTarget) {
        if (getEntityWorld() != teleportTarget.world()) {
            endJob(false);
        }

        return super.teleportTo(teleportTarget);
    }

    @Override
    protected int getExperienceToDrop(ServerWorld world) {
        return 0;
    }

    @Override
    protected void dropLoot(ServerWorld serverWorld, DamageSource damageSource, boolean causedByPlayer) {
        dropItem(serverWorld, getBotType().item());
    }

    public void dropInventory(ServerWorld serverWorld) {
        for (ItemStack stack : inventory) {
            dropStack(serverWorld, stack);
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        if (!(getEntityWorld() instanceof ServerWorld serverWorld))
            return;

        getTeamUuid().ifPresent(teamUuid -> {
            BotNetworkManager.unreserve(serverWorld, teamUuid, uuid);
        });
        endJob(false);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);

        Inventories.readData(view, inventory);
        itemsToPickUp.clear();
        itemsToPickUp.addAll(view.read(ITEMS_TO_PICK_UP_KEY, Uuids.CODEC.listOf()).orElse(List.of()));
    }

    @Override
    public void writeData(WriteView view) {
        super.writeData(view);

        Inventories.writeData(view, inventory);
        view.put(ITEMS_TO_PICK_UP_KEY, Uuids.CODEC.listOf(), itemsToPickUp.stream().toList());
    }
    //#endregion

    //#region ListInventory
    @Override
    public DefaultedList<ItemStack> getHeldStacks() {
        return inventory;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return isAlive() && player.getEntityPos().isInRange(getEntityPos(), player.getEntityInteractionRange());
    }

    @Override
    public void markDirty() {
    }
    //#endregion

    public static BotEntity spawnWithTeam(EntityType<? extends BotEntity> botEntityType, ServerWorld serverWorld, BlockPos pos, SpawnReason reason, UUID teamUuid) {
        BotEntity bot = botEntityType.create(serverWorld, reason);
        bot.refreshPositionAndAngles(pos, 0F, 0F);
        bot.initialize(serverWorld, serverWorld.getLocalDifficulty(pos), reason, null);
        bot.teamUuid = Optional.of(teamUuid);

        serverWorld.spawnEntity(bot);

        return bot;
    }
}
