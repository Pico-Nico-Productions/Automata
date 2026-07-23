package pro.piconico.automata.entity;

import java.util.Optional;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pro.piconico.automata.block.BlockUtils;
import pro.piconico.automata.bot.job.BotJob;

public class ConstructionBotEntity extends BeeEntity {
    public static final double SPEED = 1;

    @FunctionalInterface
    public interface EndJob {
        void onEnded(boolean completed);
    }

    public final Event<EndJob> JOB_ENDED = EventFactory.createArrayBacked(EndJob.class, callbacks -> (completed) -> {
        for (EndJob callback : callbacks) {
            callback.onEnded(completed);
        }
    });

    private Optional<BotJob> job;

    public ConstructionBotEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super(entityType, world);
    }

    public Optional<BotJob> getJob() {
        return job;
    }

    public void setJob(Optional<BotJob> job) {
        if (this.job.isPresent())
            JOB_ENDED.invoker().onEnded(false);

        this.job = job;
    }

    public boolean hasJob() {
        return job.isPresent();
    }

    @Override
    protected void initGoals() {
        // Removes inhereted bee behavior
    }

    @Override
    public void tick() {
        super.tick();

        if (getEntityWorld().isClient() || job.isEmpty())
            return;

        if (getBlockPos().getChebyshevDistance(job.get().pos()) > 1) {
            moveToTarget(job.get().pos());
        }
        else {
            ServerWorld serverWorld = (ServerWorld)getEntityWorld();
            if (BlockUtils.hasBreakableBlock(serverWorld, job.get().pos())) {
                serverWorld.breakBlock(job.get().pos(), true, null);
            }
            if (BlockUtils.hasFluidSourceBlock(serverWorld, job.get().pos())) {
                serverWorld.setBlockState(job.get().pos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            }
            job = Optional.empty();
            JOB_ENDED.invoker().onEnded(true);
        }
    }

    private void moveToTarget(BlockPos targetPos) {
        Vec3d targetVec = Vec3d.ofCenter(targetPos);
        getLookControl().lookAt(targetVec.x, targetVec.y, targetVec.z);

        Vec3d moveVec = targetVec.subtract(getEntityPos()).normalize().multiply(SPEED);
        setVelocity(moveVec);
        velocityDirty = true;
    }
}
