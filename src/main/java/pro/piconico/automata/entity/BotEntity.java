package pro.piconico.automata.entity;

import java.util.Optional;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import pro.piconico.automata.bot.job.BotJob;

public abstract class BotEntity extends BeeEntity {
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

    public BotEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super(entityType, world);
    }

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
    
    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        endJob(false);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        // TODO: Add BotJob codec registry
    }

    @Override
    public void writeData(WriteView view) {
        super.writeData(view);
        // TODO: Add BotJob codec registry
    }
}
