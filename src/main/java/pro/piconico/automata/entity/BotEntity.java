package pro.piconico.automata.entity;

import java.util.Optional;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.world.BotPersistentState;

// TODO: Extend PathAwareEntity instead and create goals
public abstract class BotEntity extends BeeEntity {
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

    private Optional<BotJob> job = Optional.empty();
    private Optional<RoboportBlockEntity> roboport = Optional.empty();

    public BotEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super(entityType, world);
    }

    public abstract BotType getBotType();

    public Optional<BotJob> getJob() {
        return job;
    }

    public boolean hasJob() {
        return job.isPresent();
    }

    public void endJob(boolean completed) {
        if (job.isEmpty())
            return;

        JOB_ENDED.invoker().onEnded(this, job.get(), completed);
        job = Optional.empty();
    }

    public void setJob(Optional<BotJob> job) {
        endJob(false);

        this.job = job;
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
            roboport = BotPersistentState.getRoboportClosestTo(getBlockPos(), port -> InventoryUtils.canAdd(port, getBotType().item()), serverWorld);
            if (roboport.isEmpty()) return false;
        }

        if (!navigateTo(roboport.get().getPos()))
            return false;

        roboport.get().tryAdmit(this);

        return true;
    }

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
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        endJob(false);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);

        job = view.read(JOB_KEY, BotJob.CODEC);
    }

    @Override
    public void writeData(WriteView view) {
        super.writeData(view);

        if (job.isEmpty())
            return;

        view.put(JOB_KEY, BotJob.CODEC, job.get());
    }
}
