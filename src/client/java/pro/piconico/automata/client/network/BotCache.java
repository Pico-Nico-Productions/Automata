package pro.piconico.automata.client.network;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;

public class BotCache {
    public static final Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> jobAssignments = new HashMap<>();
}
