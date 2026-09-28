package pro.piconico.automata.entity.ai.goal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;

public abstract class GoToNearestTargetGoal<EntityT extends PathAwareEntity, TargetT> extends Goal {
    protected final EntityT entity;
    private final double speed;
    private final int desiredDistance;
    protected Optional<TargetT> target = Optional.empty();

    public GoToNearestTargetGoal(EntityT entity, double speed, int desiredDistance) {
        this.entity = entity;
        this.speed = speed;
        this.desiredDistance = desiredDistance;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    protected abstract Stream<TargetT> streamTargets();

    protected abstract boolean isValidTarget(TargetT target);

    protected abstract BlockPos getPos(TargetT target);

    protected final boolean reachedDesiredDistance() {
        return entity.getBlockPos().getChebyshevDistance(getPos(target.get())) <= desiredDistance;
    }

    @Override
    public boolean canStart() {
        target = streamTargets().filter(this::isValidTarget).min(Comparator.comparingDouble(t -> entity.getBlockPos().getSquaredDistance(getPos(t))));

        return target.isPresent();
    }

    @Override
    public void stop() {
        entity.getNavigation().stop();
        target = Optional.empty();
    }

    @Override
    public boolean shouldContinue() {
        return target.map(this::isValidTarget).isPresent() || canStart();
    }

    @Override
    public void tick() {
        BlockPos targetPos = getPos(target.get());

        if (entity.getNavigation().isIdle() || !targetPos.equals(entity.getNavigation().getTargetPos())) {
            entity.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speed);
        }

        entity.getLookControl().lookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
    }
}
