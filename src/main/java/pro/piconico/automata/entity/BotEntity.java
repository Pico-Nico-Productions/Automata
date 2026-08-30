package pro.piconico.automata.entity;

import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.inventory.InventoryUtils;

// TODO: Extend PathAwareEntity instead and create goals
public abstract class BotEntity extends BeeEntity {
    private static final String TEAM_UUID_KEY = "team_uuid";
    private static final String JOB_KEY = "job";

    public static final double SPEED = 0.5;
    public static final int INTERACT_DISTANCE = 1;

    @FunctionalInterface
    public interface EndJob {
        void onEnded(BotEntity bot, BotJob job, boolean completed);
    }

    public static final Event<EndJob> JOB_ENDED = EventFactory.createArrayBacked(EndJob.class, callbacks -> (bot, job, completed) -> {
        for (EndJob callback : callbacks) {
            callback.onEnded(bot, job, completed);
        }
    });

    private UUID teamUuid;
    private Optional<BotJob> job = Optional.empty();
    private Optional<RoboportBlockEntity> roboport = Optional.empty();

    public BotEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super(entityType, world);
    }

    public abstract BotType getBotType();

    public UUID getTeamUuid() {
        return teamUuid;
    }

    public Optional<BotJob> getJob() {
        return job;
    }

    public boolean hasJob() {
        return job.isPresent();
    }

    public boolean canDoJob(BotJob job) {
        return getBotType().supportedJobTypes().contains(job.getType());
    }

    private void assertStateIsLegal() {
        if (teamUuid == null)
            throw new IllegalStateException("Bot has no team. Was this spawned without the static factory method?");
    }

    public void endJob(boolean completed) {
        if (job.isEmpty())
            return;

        BotJob oldJob = job.get();
        job = Optional.empty();
        JOB_ENDED.invoker().onEnded(this, oldJob, completed);
    }

    public boolean setJob(Optional<BotJob> newJob) {
        if (newJob.isPresent() && !canDoJob(newJob.get()))
            return false;

        Optional<BotJob> oldJob = job;
        job = newJob;
        if (oldJob.isPresent())
            JOB_ENDED.invoker().onEnded(this, oldJob.get(), false);

        return true;
    }

    private boolean navigateTo(BlockPos pos) {
        if (getBlockPos().getSquaredDistance(pos) > INTERACT_DISTANCE * INTERACT_DISTANCE) {
            if (pos.equals(getNavigation().getTargetPos()))
                return false;

            getNavigation().startMovingTo(pos.getX(), pos.getY(), pos.getZ(), SPEED);

            return false;
        }

        return true;
    }

    private boolean doJob(ServerWorld serverWorld) {
        if (job.isEmpty())
            return true;

        if (!navigateTo(job.get().pos()))
            return false;

        endJob(job.get().execute(serverWorld));

        return true;
    }

    private boolean returnToPort(ServerWorld serverWorld) {
        if (roboport.isEmpty() || !InventoryUtils.canAdd(roboport.get(), getBotType().item())) {
            roboport = RoboportBlockEntity.getClosestTo(getBlockPos(), RoboportBlockEntity.CHUNK_RANGE + 1, port -> InventoryUtils.canAdd(port, getBotType().item()), serverWorld);
            if (roboport.isEmpty())
                return false;
        }

        if (!navigateTo(roboport.get().getPos()))
            return false;

        roboport.get().tryAdd(this);

        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!(getEntityWorld() instanceof ServerWorld serverWorld))
            return;

        assertStateIsLegal();

        if (!doJob(serverWorld))
            return;

        returnToPort(serverWorld);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        endJob(false);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);

        teamUuid = view.read(TEAM_UUID_KEY, Uuids.INT_STREAM_CODEC).get();
        job = view.read(JOB_KEY, BotJob.CODEC);
    }

    @Override
    public void writeData(WriteView view) {
        super.writeData(view);

        view.put(TEAM_UUID_KEY, Uuids.INT_STREAM_CODEC, teamUuid);

        if (job.isEmpty())
            return;

        view.put(JOB_KEY, BotJob.CODEC, job.get());
    }

    public static BotEntity spawn(EntityType<? extends BotEntity> botEntityType, ServerWorld serverWorld, BlockPos pos, UUID teamUuid) {
        BotEntity newBotEntity = botEntityType.spawn(serverWorld, pos, SpawnReason.MOB_SUMMONED);
        newBotEntity.teamUuid = teamUuid;

        return newBotEntity;
    }
}
