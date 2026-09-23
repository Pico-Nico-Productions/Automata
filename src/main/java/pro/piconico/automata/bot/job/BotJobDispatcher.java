package pro.piconico.automata.bot.job;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.block.BlockUtils;
import pro.piconico.automata.world.BotJobPersistentState;

public class BotJobDispatcher {
    public static Optional<Integer> markForDeconstruction(ServerWorld serverWorld, UUID teamUuid, BlockPos selection1, BlockPos selection2) {
        BlockPos min = BlockPos.min(selection1, selection2);
        BlockPos max = BlockPos.max(selection1, selection2);
        ArrayList<BotJob> jobsToAdd = new ArrayList<>();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (!BlockUtils.hasDeconstructableBlock(serverWorld, blockPos))
                        continue;

                    jobsToAdd.add(new DeconstructionBotJob(blockPos));
                }
            }
        }
        Optional<Integer> jobCount = BotJobPersistentState.addJobs(teamUuid, jobsToAdd, serverWorld);

        return jobCount;
    }
}
