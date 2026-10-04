package pro.piconico.automata.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.ListInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.ai.goal.BotDoJobGoal;
import pro.piconico.automata.entity.ai.goal.BotGrabJobItemsGoal;
import pro.piconico.automata.entity.ai.goal.BotHoverGoal;
import pro.piconico.automata.entity.ai.goal.BotPickUpItemsGoal;
import pro.piconico.automata.entity.ai.goal.BotStoreItemsGoal;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.util.math.ChunkUtils;
import pro.piconico.automata.entity.ai.goal.BotReturnToRoboportGoal;
import pro.piconico.automata.world.BotJobPersistentState;
import pro.piconico.automata.world.BotTeamPersistentState;

public abstract class BotEntity extends PathAwareEntity implements ListInventory {
    private static final String TEAM_UUID_KEY = "team_uuid";
    public static final int MAX_HEALTH = 10;
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

    private Optional<UUID> teamUuid = Optional.empty();
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final Set<ItemEntity> itemsToPickUp = new HashSet<>();

    public BotEntity(EntityType<? extends BotEntity> entityType, World world) {
        super(entityType, world);
        moveControl = new FlightMoveControl(this, getMaxLookPitchChange(), true);
    }

    public static DefaultAttributeContainer.Builder createBotAttributes() {
        return PathAwareEntity.createMobAttributes() //
                .add(EntityAttributes.MAX_HEALTH, MAX_HEALTH).add(EntityAttributes.MOVEMENT_SPEED, SPEED)//
                .add(EntityAttributes.FLYING_SPEED, SPEED);
    }

    public abstract BotType getBotType();

    public boolean isOnTeam(UUID teamUuid) {
        return this.teamUuid.filter(uuid -> uuid.equals(teamUuid)).isPresent();
    }

    public Optional<UUID> getTeamUuid() {
        return teamUuid;
    }

    public void setTeamUuid(Optional<UUID> teamUuid) {
        if (this.teamUuid.equals(teamUuid))
            return;

        endJob(false);

        this.teamUuid = teamUuid;
    }

    public Optional<BotJob> getJob() {
        return BotJobPersistentState.getJob((ServerWorld)getEntityWorld(), uuid);
    }

    public boolean canDoJob(BotJob job) {
        return getJob().isEmpty() && getBotType().supportedJobTypes().contains(job.getType()) && InventoryUtils.hasStacks(this, job.getRequiredStacks(getEntityWorld()));
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
        return Collections.unmodifiableSet(itemsToPickUp);
    }

    public void addItemsToPickUp(Collection<ItemEntity> items) {
        itemsToPickUp.addAll(items);
    }

    public void removeInvalidItemsToPickUp() {
        itemsToPickUp.removeIf(item -> item.isRemoved());
    }

    //#region PathAwareEntity
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
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        // TODO: Implement BotDevice
        return super.interactMob(player, hand);
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

        if (getEntityWorld().isClient())
            return;

        endJob(false);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);

        teamUuid = view.read(TEAM_UUID_KEY, Uuids.INT_STREAM_CODEC).filter(uuid -> BotTeamPersistentState.getTeam(uuid).isPresent());
        Inventories.readData(view, inventory);
    }

    @Override
    public void writeData(WriteView view) {
        super.writeData(view);

        teamUuid.ifPresent(uuid -> view.put(TEAM_UUID_KEY, Uuids.INT_STREAM_CODEC, uuid));
        Inventories.writeData(view, inventory);
    }
    //#endregion

    //#region ListInventory
    @Override
    public DefaultedList<ItemStack> getHeldStacks() {
        return inventory;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void markDirty() {
    }
    //#endregion
}
