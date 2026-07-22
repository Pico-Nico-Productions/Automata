package pro.piconico.automata.bot;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.block.BlockUtils;
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
        ArrayList<BlockPos> jobsToRemove = new ArrayList<>();
        for (BlockPos blockPos : botState.getDeconstructionJobs()) {
            if (!canDeconstruct(serverWorld, blockPos)) {
                jobsToRemove.add(blockPos);

                continue;
            }

            //#region TODO: Replace with roboport search and bot dispatch
            if (BlockUtils.hasBreakableBlock(serverWorld, blockPos)) {
                serverWorld.breakBlock(blockPos, true, null);
            }
            if (BlockUtils.hasFluidSourceBlock(serverWorld, blockPos)) {
                serverWorld.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            }
            jobsToRemove.add(blockPos);
            //#endregion
        }
        botState.removeDeconstructionJobs(jobsToRemove, serverWorld);
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
        ArrayList<BlockPos> jobsToAdd = new ArrayList<>();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (!canDeconstruct(world, blockPos))
                        continue;

                    jobsToAdd.add(blockPos);
                }
            }
        }
        BotPersistentState botState = AutomataPersistentStates.get(world, AutomataPersistentStates.BOT_PERSISTENT_STATE);
        botState.addDeconstructionJobs(jobsToAdd, (ServerWorld)world);

        player.sendMessage(Text.translatable(AutomataTexts.DECONSTRUCTION, selection1.toShortString(), selection2.toShortString()), true);

        return ActionResult.SUCCESS;
    }

    public static void initialize() {
        BotPersistentState.ROBOPORTS_MUTATE.register((serverWorld) -> update(serverWorld));
        BotPersistentState.DECONSTRUCTION_JOBS_MUTATE.register((serverWorld) -> update(serverWorld));
    }
}
