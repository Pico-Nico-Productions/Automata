package pro.piconico.automata.bot;

import java.util.ArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.block.BlockUtils;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.DeconstructionJob;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.component.CommandToolComponent;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotJobPersistentState;

public class BotDispatcher {
    public static ActionResult markForDeconstruction(PlayerEntity player, CommandToolComponent commandToolComponent) {
        World world = player.getEntityWorld();
        if (world.isClient()) {
            return commandToolComponent.hasSelection() && commandToolComponent.teamUuid().isPresent() ? ActionResult.SUCCESS : ActionResult.FAIL;
        }

        if (!commandToolComponent.hasSelection()) {
            player.sendMessage(Text.translatable(AutomataTexts.DECONSTRUCTION_FAILED), true);

            return ActionResult.FAIL;
        }

        if (commandToolComponent.teamUuid().isEmpty()) {
            player.sendMessage(Text.translatable(AutomataTexts.TEAM_MISSING, BotTeam.EMPTY_BOT_TEAM_STRING), true);

            return ActionResult.FAIL;
        }

        BlockPos selection1 = commandToolComponent.selection1().get();
        BlockPos selection2 = commandToolComponent.selection2().get();
        BlockPos min = BlockPos.min(selection1, selection2);
        BlockPos max = BlockPos.max(selection1, selection2);
        ArrayList<BotJob> jobsToAdd = new ArrayList<>();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (!BlockUtils.hasDeconstructableBlock(world, blockPos))
                        continue;

                    jobsToAdd.add(new DeconstructionJob(blockPos));
                }
            }
        }
        long jobCount = BotJobPersistentState.addJobs(commandToolComponent.teamUuid().get(), jobsToAdd, (ServerWorld)world);

        player.sendMessage(Text.translatable(AutomataTexts.JOBS_ADDED, jobCount), true);

        return ActionResult.SUCCESS;
    }
}
