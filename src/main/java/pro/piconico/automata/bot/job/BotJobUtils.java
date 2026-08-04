package pro.piconico.automata.bot.job;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;

public class BotJobUtils {
    public static final Codec<Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>>> JOB_ASSIGNMENT_CODEC = BotJobAssignment.CODEC.listOf().xmap(BotJobUtils::map,
            BotJobUtils::flatten);

    public static final PacketCodec<ByteBuf, Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>>> JOB_ASSIGNMENT_PACKET_CODEC = PacketCodecs.codec(JOB_ASSIGNMENT_CODEC);

    public static final List<BotJobAssignment> flatten(Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> jobAssignmentMap) {
        return jobAssignmentMap.values().stream().flatMap(typeMap -> typeMap.values().stream()).toList();
    }

    public static final Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> map(Collection<BotJobAssignment> jobAssignments) {
        return jobAssignments.stream().collect(Collectors.groupingBy(assignment -> assignment.getJob().pos(), HashMap::new,
                Collectors.toMap(assignment -> assignment.getJob().getType(), assignment -> assignment, (existing, replacement) -> replacement, HashMap::new)));
    }
}
