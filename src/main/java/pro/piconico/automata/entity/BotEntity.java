package pro.piconico.automata.entity;

import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.entity.ai.goal.BotDoJobGoal;
import pro.piconico.automata.entity.ai.goal.BotHoverGoal;
import pro.piconico.automata.entity.ai.goal.PickUpItemGoal;
import pro.piconico.automata.entity.ai.goal.StoreItemsGoal;
import pro.piconico.automata.entity.ai.goal.BotReturnToRoboportGoal;
import pro.piconico.automata.world.BotJobPersistentState;
import pro.piconico.automata.world.BotTeamPersistentState;

public abstract class BotEntity extends PathAwareEntity implements ListInventory {
    private static final String TEAM_UUID_KEY = "team_uuid";
    public static final double MAX_HEALTH = 10.0;
    public static final int INVENTORY_SIZE = 3;
    public static final double SPEED = 1.0;
    public static final double SEARCH_SCALE = 2.0;
    public static final int INTERACT_DISTANCE = 1;
    public static final double PICK_UP_RADIUS = 1.0;

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
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

    public BotEntity(EntityType<? extends BotEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createBotAttributes() {
        return PathAwareEntity.createLivingAttributes().add(EntityAttributes.MAX_HEALTH, MAX_HEALTH).add(EntityAttributes.MOVEMENT_SPEED, SPEED);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new BotDoJobGoal(this, SPEED, INTERACT_DISTANCE));
        goalSelector.add(1, new PickUpItemGoal(this, this, SEARCH_SCALE, SPEED, PICK_UP_RADIUS));
        goalSelector.add(2, new StoreItemsGoal(this));
        goalSelector.add(3, new BotReturnToRoboportGoal(this, SPEED, INTERACT_DISTANCE));
        goalSelector.add(4, new BotHoverGoal());
    }

    public abstract BotType getBotType();

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
        return getJob().isEmpty() && getBotType().supportedJobTypes().contains(job.getType()) && job.canExecute((ServerWorld)getEntityWorld(), this);
    }

    public void endJob(boolean completed) {
        Optional<BotJob> job = getJob();

        if (job.isEmpty())
            return;

        JOB_ENDED.invoker().onEnded(this, job.get(), completed);
    }

    //#region PathAwareEntity
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
        for (ItemStack stack : inventory) {
            dropStack(serverWorld, stack);
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

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
