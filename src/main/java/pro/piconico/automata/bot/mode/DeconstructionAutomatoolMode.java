package pro.piconico.automata.bot.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.DeconstructionBotJob;
import pro.piconico.automata.component.BotTeamComponent;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.registry.AutomataAutomatoolModes;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.util.math.BlockPosUtils;
import pro.piconico.automata.world.BotJobPersistentState;

public class DeconstructionAutomatoolMode {
    public static void dispatch(Optional<ServerPlayerEntity> serverPlayer, UUID teamUuid, ServerWorld serverWorld, List<BlockPos> selections) {
        if (selections.isEmpty()) {
            serverPlayer.ifPresent(player -> player.sendMessage(AutomataTexts.getSelectionInvalid(), true));
            return;
        }

        BlockPos min = BlockPosUtils.min(selections).get(), max = BlockPosUtils.max(selections).get();
        ArrayList<BotJob> jobsToAdd = new ArrayList<>();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    DeconstructionBotJob deconstructionJob = new DeconstructionBotJob(new BlockPos(x, y, z));
                    if (!deconstructionJob.canStart(serverWorld))
                        continue;

                    jobsToAdd.add(deconstructionJob);
                }
            }
        }
        int jobCount = BotJobPersistentState.addJobs(teamUuid, jobsToAdd, serverWorld);

        serverPlayer.ifPresent(player -> player.sendMessage(AutomataTexts.getBotJobsAdded(jobCount), true));
    }

    public static void dispatch(ServerPlayerEntity serverPlayer, ItemStack stack) {
        if (!AutomataAutomatoolModes.DECONSTRUCTION.canDispatch(serverPlayer, stack, true))
            return;

        UUID teamUuid = BotTeamComponent.get(stack).get();
        SelectionComponent selection = SelectionComponent.get(stack).get();
        ServerWorld serverWorld = selection.getWorld(serverPlayer).get();
        dispatch(Optional.of(serverPlayer), teamUuid, serverWorld, selection.getSelections());
    }
}
