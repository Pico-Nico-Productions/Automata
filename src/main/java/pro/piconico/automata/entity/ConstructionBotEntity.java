package pro.piconico.automata.entity;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.Automata;
import pro.piconico.automata.block.BlockUtils;
import pro.piconico.automata.bot.job.DeconstructionJob;

public class ConstructionBotEntity extends BotEntity {
    public ConstructionBotEntity(EntityType<? extends BotEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (getEntityWorld().isClient())
            return;

        if (getJob().isEmpty()) {
            // TODO: Navigate back to roboport
            return;
        }

        BlockPos jobPos = getJob().get().pos();
        switch (getJob().get()) {
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
            endJob(false);
            break;
        }
    }
}
