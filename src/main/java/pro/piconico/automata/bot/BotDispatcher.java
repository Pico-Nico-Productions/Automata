package pro.piconico.automata.bot;

import java.util.ArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.block.BlockUtils;
import pro.piconico.automata.bot.job.DeconstructionJob;
import pro.piconico.automata.component.CommandToolComponent;
import pro.piconico.automata.registry.AutomataPersistentStates;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotPersistentState;

public class BotDispatcher {
    private static boolean canDeconstruct(World world, BlockPos blockPos) {
        return BlockUtils.hasBreakableBlock(world, blockPos) || BlockUtils.hasFluidSourceBlock(world, blockPos);
    }

    public static void update(ServerWorld serverWorld) {
        BotPersistentState botState = AutomataPersistentStates.get(serverWorld, AutomataPersistentStates.BOT_PERSISTENT_STATE);
        ArrayList<DeconstructionJob> jobsToRemove = new ArrayList<>(), jobsToAssign = new ArrayList<>();
        for (DeconstructionJob job : botState.getUnassignedDeconstructionJobs()) {
            if (!canDeconstruct(serverWorld, job.pos())) {
                jobsToRemove.add(job);

                continue;
            }

            jobsToAssign.add(job);
        }
        botState.removeDeconstructionJobs(jobsToRemove, serverWorld);
        botState.assignDeconstructionJobsToClosestRoboportInRange(jobsToAssign, serverWorld);
    }

    public static ActionResult markForDeconstruction(PlayerEntity player, CommandToolComponent commandToolComponent) {
        World world = player.getEntityWorld();
        if (world.isClient()) {
            return commandToolComponent.hasSelection() ? ActionResult.SUCCESS : ActionResult.FAIL;
        }

        if (!commandToolComponent.hasSelection()) {
            player.sendMessage(Text.translatable(AutomataTexts.INVALID_DECONSTRUCTION_SELECTION), true);

            return ActionResult.FAIL;
        }

        BlockPos selection1 = commandToolComponent.selection1().get();
        BlockPos selection2 = commandToolComponent.selection2().get();
        BlockPos min = BlockPos.min(selection1, selection2);
        BlockPos max = BlockPos.max(selection1, selection2);
        ArrayList<DeconstructionJob> jobsToAdd = new ArrayList<>();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (!canDeconstruct(world, blockPos))
                        continue;

                    jobsToAdd.add(new DeconstructionJob(blockPos));
                }
            }
        }
        BotPersistentState botState = AutomataPersistentStates.get(world, AutomataPersistentStates.BOT_PERSISTENT_STATE);
        botState.addDeconstructionJobs(jobsToAdd, (ServerWorld)world);

        player.sendMessage(Text.translatable(AutomataTexts.DECONSTRUCTION, selection1.toShortString(), selection2.toShortString()), true);

        return ActionResult.SUCCESS;
    }

    public static void initialize() {
        BotPersistentState.ROBOPORTS_MUTATED.register((serverWorld) -> update(serverWorld));
        BotPersistentState.DECONSTRUCTION_JOBS_MUTATED.register((serverWorld) -> update(serverWorld));
    }
}
