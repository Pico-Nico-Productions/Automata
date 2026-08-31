package pro.piconico.automata.bot.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.registry.AutomataRegistries;
import pro.piconico.automata.util.MapUtils;

// TODO: Denest team UUID and move it to BotJobPersistentState because this map is meant to represent a single world single team view
public class BotJobAssignmentMap extends HashMap<UUID, Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>>> {
    public static final Codec<BotJobAssignmentMap> CODEC = BotJobAssignment.CODEC.listOf().xmap(BotJobAssignmentMap::new, BotJobAssignmentMap::flatten);
    public static final PacketCodec<ByteBuf, BotJobAssignmentMap> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(HashMap::new, Uuids.PACKET_CODEC,
                    PacketCodecs.optional(PacketCodecs.map(HashMap::new, BlockPos.PACKET_CODEC,
                            PacketCodecs.optional(PacketCodecs.map(HashMap::new, PacketCodecs.codec(AutomataRegistries.BOT_JOB_TYPE.getCodec()),
                                    PacketCodecs.optional(PacketCodecs.codec(BotJobAssignment.CODEC))))))), //
            BotJobAssignmentMap::toOptionalMap, //
            BotJobAssignmentMap::fromOptionalMap);

    public BotJobAssignmentMap() {
        super();
    }

    public BotJobAssignmentMap(Iterable<BotJobAssignment> jobAssignments) {
        super();

        for (BotJobAssignment jobAssignment : jobAssignments) {
            Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> blockMap = computeIfAbsent(jobAssignment.TEAM_UUID, uuid -> new HashMap<>());
            Map<BotJobType<?>, BotJobAssignment> typeMap = blockMap.computeIfAbsent(jobAssignment.JOB.pos(), pos -> new HashMap<>());
            typeMap.put(jobAssignment.JOB.getType(), jobAssignment);
        }
    }

    public BotJobAssignmentMap(BotJobAssignmentMap original) {
        super(original);
    }

    private static List<BotJobAssignment> flatten(BotJobAssignmentMap jobAssignmentMap) {
        return jobAssignmentMap.values().stream().flatMap(blockMap -> blockMap.values().stream()).flatMap(typeMap -> typeMap.values().stream()).toList();
    }

    private static Map<UUID, Optional<Map<BlockPos, Optional<Map<BotJobType<?>, Optional<BotJobAssignment>>>>>> toOptionalMap(
            BotJobAssignmentMap jobAssignmentMap) {
        Function<Map<BotJobType<?>, BotJobAssignment>, Map<BotJobType<?>, Optional<BotJobAssignment>>> wrapTypeLayer = typeMap -> MapUtils
                .wrapValuesToOptional(typeMap, Function.identity());

        Function<Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>>, Map<BlockPos, Optional<Map<BotJobType<?>, Optional<BotJobAssignment>>>>> wrapBlockLayer = blockMap -> MapUtils
                .wrapValuesToOptional(blockMap, wrapTypeLayer);

        return MapUtils.wrapValuesToOptional(jobAssignmentMap, wrapBlockLayer);
    }

    private static BotJobAssignmentMap fromOptionalMap(
            Map<UUID, Optional<Map<BlockPos, Optional<Map<BotJobType<?>, Optional<BotJobAssignment>>>>>> optionalMap) {
        Function<Map<BotJobType<?>, Optional<BotJobAssignment>>, Map<BotJobType<?>, BotJobAssignment>> unwrapTypeLayer = typeMap -> MapUtils
                .unwrapOptionalValues(typeMap, Function.identity());

        Function<Map<BlockPos, Optional<Map<BotJobType<?>, Optional<BotJobAssignment>>>>, Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>>> unwrapBlockLayer = blockMap -> MapUtils
                .unwrapOptionalValues(blockMap, unwrapTypeLayer);

        Map<UUID, Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>>> map = MapUtils.unwrapOptionalValues(optionalMap, unwrapBlockLayer);
        BotJobAssignmentMap jobAssignmentMap = new BotJobAssignmentMap();

        if (map != null) {
            jobAssignmentMap.putAll(map);
        }

        return jobAssignmentMap;
    }

    public BotJobAssignmentMap calculateDelta(BotJobAssignmentMap newMap) {
        BiFunction<Map<BotJobType<?>, BotJobAssignment>, Map<BotJobType<?>, BotJobAssignment>, Map<BotJobType<?>, BotJobAssignment>> deltaTypeMap = (oldType,
                newType) -> MapUtils.computeMapDelta(oldType, newType, (oldVal, newVal) -> Objects.equals(oldVal, newVal) ? null : newVal);

        BiFunction<Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>>, Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>>, Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>>> deltaBlockMap = (
                oldBlock, newBlock) -> MapUtils.computeMapDelta(oldBlock, newBlock, deltaTypeMap);

        Map<UUID, Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>>> deltaMap = MapUtils.computeMapDelta(this, newMap, deltaBlockMap);
        BotJobAssignmentMap jobAssignmentMap = new BotJobAssignmentMap();

        if (deltaMap != null) {
            jobAssignmentMap.putAll(deltaMap);
        }

        return jobAssignmentMap;
    }
}
