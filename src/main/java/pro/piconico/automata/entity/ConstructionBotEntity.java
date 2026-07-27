package pro.piconico.automata.entity;

import java.util.Optional;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.Automata;
import pro.piconico.automata.block.BlockUtils;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.DeconstructionJob;

public class ConstructionBotEntity extends BeeEntity {
    public static final double SPEED = 0.5;
    public static final int INTERACT_DISTANCE = 1;

    @FunctionalInterface
    public interface EndJob {
        void onEnded(ConstructionBotEntity bot, BotJob job, boolean completed);
    }

    public static final Event<EndJob> JOB_ENDED = EventFactory.createArrayBacked(EndJob.class, callbacks -> (bot, job, completed) -> {
        for (EndJob callback : callbacks) {
            callback.onEnded(bot, job, completed);
        }
    });

    private Optional<BotJob> job = Optional.empty();

    public ConstructionBotEntity(EntityType<? extends BeeEntity> entityType, World world) {
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
    public void tick() {
        super.tick();

        if (getEntityWorld().isClient())
            return;

        if (job.isEmpty()) {
            // TODO: Navigate back to roboport
            return;
        }

        BlockPos jobPos = job.get().pos();
        switch (job.get()) {
        case DeconstructionJob deconstructionJob:
            if (!BlockUtils.hasDeconstructableBlock(getEntityWorld(), jobPos)) {
                endJob(true);
                return;
            }

            if (getBlockPos().getSquaredDistance(jobPos) > INTERACT_DISTANCE * INTERACT_DISTANCE) {
                if (jobPos.equals(getNavigation().getTargetPos()))
                    return;

                getNavigation().startMovingTo(jobPos.getX(), jobPos.getY(), jobPos.getZ(), SPEED);
                
                return;
            }

            ServerWorld serverWorld = (ServerWorld)getEntityWorld();
            if (BlockUtils.hasBreakableBlock(serverWorld, jobPos)) {
                serverWorld.breakBlock(jobPos, true, null);
            }
            if (BlockUtils.hasFluidSourceBlock(serverWorld, jobPos)) {
                serverWorld.setBlockState(jobPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            }

            endJob(true);
            break;
        default:
            Automata.logError(ConstructionBotEntity.class.getSimpleName() + " was assigned an unhandled job type", IllegalStateException::new);
            break;
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        setJob(Optional.empty());
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
