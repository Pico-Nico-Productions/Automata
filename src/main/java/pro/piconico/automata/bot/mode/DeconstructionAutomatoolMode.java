package pro.piconico.automata.bot.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.DeconstructionBotJob;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.registry.AutomataAutomatoolModes;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.util.math.BlockPosUtils;
import pro.piconico.automata.world.BotJobPersistentState;

public class DeconstructionAutomatoolMode {
    public static Text dispatch(UUID teamUuid, ServerWorld serverWorld, List<BlockPos> selections) {
        if (selections.isEmpty())
            return AutomataTexts.getSelectionInvalid();

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

        return AutomataTexts.getBotJobsAdded(jobCount);
    }

    public static Optional<Text> dispatch(ServerPlayerEntity serverPlayer, Optional<UUID> teamUuid, Optional<SelectionComponent> selection) {
        Optional<Text> requirementFailure = AutomatoolMode.getRequirementFailure(AutomataAutomatoolModes.DECONSTRUCTION, serverPlayer, teamUuid, selection);
        if (requirementFailure.isPresent())
            return requirementFailure;

        return Optional.of(dispatch(teamUuid.get(), selection.get().getWorld(serverPlayer).get(), selection.get().getSelections()));
    }
}
