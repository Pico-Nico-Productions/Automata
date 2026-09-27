package pro.piconico.automata.entity;

import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.ListInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.world.BotJobPersistentState;
import pro.piconico.automata.world.BotTeamPersistentState;

// TODO: Extend PathAwareEntity instead and create goals
public abstract class BotEntity extends BeeEntity implements ListInventory {
    private static final String TEAM_UUID_KEY = "team_uuid";
    public static final double MAX_HEALTH = 10.0;
    public static final double SPEED = 1.0;
    public static final int INTERACT_DISTANCE = 1;
    public static final int INVENTORY_SIZE = 3;

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
    private Optional<RoboportBlockEntity> roboport = Optional.empty();

    public BotEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createBotAttributes() {
        return BeeEntity.createBeeAttributes().add(EntityAttributes.MAX_HEALTH, MAX_HEALTH).add(EntityAttributes.FLYING_SPEED, SPEED);
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

    private Optional<BotJob> getJob() {
        return BotJobPersistentState.getJob((ServerWorld)getEntityWorld(), uuid);
    }

    public boolean canDoJob(BotJob job) {
        return getJob().isEmpty() && getBotType().supportedJobTypes().contains(job.getType()) && job.canExecute((ServerWorld)getEntityWorld(), this);
    }

    private void endJob(boolean completed) {
        Optional<BotJob> job = getJob();

        if (job.isEmpty())
            return;

        JOB_ENDED.invoker().onEnded(this, job.get(), completed);
    }

    private boolean navigateTo(BlockPos pos) {
        if (getBlockPos().getChebyshevDistance(pos) > INTERACT_DISTANCE) {
            if (pos.equals(getNavigation().getTargetPos()))
                return false;

            getNavigation().startMovingTo(pos.getX(), pos.getY(), pos.getZ(), SPEED);

            return false;
        }

        return true;
    }

    private boolean doJob(ServerWorld serverWorld) {
        Optional<BotJob> job = getJob();

        if (job.isEmpty())
            return true;

        if (!navigateTo(job.get().pos()))
            return false;

        endJob(job.get().execute(serverWorld, this));

        return true;
    }

    private boolean returnToPort(ServerWorld serverWorld) {
        if (teamUuid.isEmpty())
            return false;

        if (roboport.isEmpty() || !InventoryUtils.canAdd(roboport.get(), getBotType().item())) {
            roboport = RoboportBlockEntity.getClosestTo(getBlockPos(), port -> InventoryUtils.canAdd(port, getBotType().item()), serverWorld);
            if (roboport.isEmpty())
                return false;
        }

        if (!navigateTo(roboport.get().getPos()))
            return false;

        roboport.get().tryAdd(this);

        return true;
    }

    //#region LivingEntity
    @Override
    public void tick() {
        super.tick();

        if (!(getEntityWorld() instanceof ServerWorld serverWorld))
            return;

        if (!doJob(serverWorld))
            return;

        returnToPort(serverWorld);
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
}
