package pro.piconico.automata.bot;

import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
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
    
    public static void update(World world) {
        BotPersistentState state = AutomataPersistentStates.get(world, AutomataPersistentStates.BOT_PERSISTENT_STATE);
        Iterator<BlockPos> deconstructionIterator = state.getDeconstructionJobs().iterator();
        while (deconstructionIterator.hasNext()) {
            BlockPos blockPos = deconstructionIterator.next();

            if (!canDeconstruct(world, blockPos)) {
                deconstructionIterator.remove();
                state.markDirty();

                continue;
            }
            
            //#region TODO: Replace with roboport search and bot dispatch
            if (BlockUtils.hasBreakableBlock(world, blockPos)) {
                world.breakBlock(blockPos, true, null);
            }
            if (BlockUtils.hasFluidSourceBlock(world, blockPos)) {
                world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            }
            deconstructionIterator.remove();
            state.markDirty();
            //#endregion
        }
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
        BotPersistentState botState = AutomataPersistentStates.get(world, AutomataPersistentStates.BOT_PERSISTENT_STATE);
        boolean jobAdded = false;
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (!canDeconstruct(world, blockPos))
                        continue;

                    botState.addDeconstructionJob(blockPos);
                    jobAdded = true;
                }
            }
        }

        player.sendMessage(Text.translatable(AutomataTexts.DECONSTRUCTION, selection1.toShortString(), selection2.toShortString()), true);

        if (jobAdded) update(world);

        return ActionResult.SUCCESS;
    }
}
