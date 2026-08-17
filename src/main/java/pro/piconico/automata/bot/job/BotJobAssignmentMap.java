package pro.piconico.automata.bot.job;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.registry.AutomataRegistries;

public class BotJobAssignmentMap extends HashMap<BlockPos, Map<BotJobType<?>, BotJobAssignment>> {
    public static final Codec<BotJobAssignmentMap> CODEC = BotJobAssignment.CODEC.listOf().xmap(BotJobAssignmentMap::map, BotJobAssignmentMap::flatten);
    public static final PacketCodec<ByteBuf, BotJobAssignmentMap> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.map(HashMap::new, BlockPos.PACKET_CODEC, PacketCodecs.optional(PacketCodecs.map(HashMap::new, PacketCodecs.codec(AutomataRegistries.BOT_JOB_TYPE.getCodec()), PacketCodecs.optional(PacketCodecs.codec(BotJobAssignment.CODEC))))), BotJobAssignmentMap::toOptionalMap, BotJobAssignmentMap::new);

    public BotJobAssignmentMap() {
        super();
    }

    public BotJobAssignmentMap(Collection<BotJobAssignment> jobAssignments) {
        super();

        for (BotJobAssignment jobAssignment : jobAssignments) {
            computeIfAbsent(jobAssignment.job.pos(), pos -> new HashMap<>()).put(jobAssignment.job.getType(), jobAssignment);
        }
    }

    public BotJobAssignmentMap(BotJobAssignmentMap original) {
        super(original);
    }

    private BotJobAssignmentMap(Map<BlockPos, Optional<Map<BotJobType<?>, Optional<BotJobAssignment>>>> optionalJobAssignmentMap) {
        for (Entry<BlockPos, Optional<Map<BotJobType<?>, Optional<BotJobAssignment>>>> jobAssignmentEntry : optionalJobAssignmentMap.entrySet()) {
            Optional<Map<BotJobType<?>, Optional<BotJobAssignment>>> optionalTypeMap = jobAssignmentEntry.getValue();

            if (optionalTypeMap.isEmpty()) {
                put(jobAssignmentEntry.getKey(), null);
                continue;
            }

            Map<BotJobType<?>, BotJobAssignment> typeMap = optionalTypeMap.get().entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().orElse(null)));
            put(jobAssignmentEntry.getKey(), typeMap);
        }
    }

    public BotJobAssignmentMap calculateDelta(BotJobAssignmentMap newMap) {
        BotJobAssignmentMap deltaMap = new BotJobAssignmentMap(newMap);

        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> jobAssignmentEntry : entrySet()) {
            BlockPos blockPos = jobAssignmentEntry.getKey();

            if (!newMap.containsKey(blockPos)) {
                deltaMap.put(blockPos, null);
                continue;
            }

            Map<BotJobType<?>, BotJobAssignment> oldTypeMap = jobAssignmentEntry.getValue(), newTypeMap = newMap.get(blockPos);

            if (oldTypeMap.equals(newTypeMap)) {
                deltaMap.remove(blockPos);
                continue;
            }

            Map<BotJobType<?>, BotJobAssignment> deltaTypeMap = new HashMap<>(newTypeMap);
            for (Entry<BotJobType<?>, BotJobAssignment> typeEntry : oldTypeMap.entrySet()) {
                BotJobType<?> type = typeEntry.getKey();

                if (!newTypeMap.containsKey(type)) {
                    deltaTypeMap.put(type, null);
                    continue;
                }

                BotJobAssignment oldJobAssignment = typeEntry.getValue(), newJobAssignment = newTypeMap.get(type);

                if (oldJobAssignment.equals(newJobAssignment)) {
                    // TODO: Ensure this doesn't run when assignment changes (Might be equal due to same reference)
                    deltaTypeMap.remove(type);
                }
            }
            deltaMap.put(blockPos, deltaTypeMap);
        }

        return deltaMap;
    }

    private static List<BotJobAssignment> flatten(BotJobAssignmentMap jobAssignmentMap) {
        return jobAssignmentMap.values().stream().flatMap(typeMap -> typeMap.values().stream()).toList();
    }

    private static BotJobAssignmentMap map(Collection<BotJobAssignment> jobAssignments) {
        return jobAssignments.stream().collect(Collectors.groupingBy(assignment -> assignment.job.pos(), BotJobAssignmentMap::new,
                Collectors.toMap(assignment -> assignment.job.getType(), assignment -> assignment, (existing, replacement) -> replacement, HashMap::new)));
    }

    private static Map<BlockPos, Optional<Map<BotJobType<?>, Optional<BotJobAssignment>>>> toOptionalMap(BotJobAssignmentMap jobAssignmentMap) {
        Map<BlockPos, Optional<Map<BotJobType<?>, Optional<BotJobAssignment>>>> optionalJobAssignmentMap = new HashMap<>();

        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> jobAssignmentEntry : jobAssignmentMap.entrySet()) {
            Map<BotJobType<?>, BotJobAssignment> typeMap = jobAssignmentEntry.getValue();

            if (typeMap == null) {
                optionalJobAssignmentMap.put(jobAssignmentEntry.getKey(), Optional.empty());
                continue;
            }

            Optional<Map<BotJobType<?>, Optional<BotJobAssignment>>> optionalTypeMap = Optional
                    .of(typeMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> Optional.ofNullable(entry.getValue()))));
            optionalJobAssignmentMap.put(jobAssignmentEntry.getKey(), optionalTypeMap);
        }

        return optionalJobAssignmentMap;
    }
}
