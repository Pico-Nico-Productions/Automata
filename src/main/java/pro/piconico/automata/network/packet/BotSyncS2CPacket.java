package pro.piconico.automata.network.packet;

import java.util.Map;
import java.util.Set;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.job.BotJobUtils;
import pro.piconico.automata.bot.network.BotNetwork;
import pro.piconico.automata.bot.network.BotNetworkManager.ServerBotNetwork;
import pro.piconico.automata.bot.network.BotNetworkUtils;
import pro.piconico.automata.registry.AutomataRegistry;

public record BotSyncS2CPacket(boolean clear, Map<ChunkPos, BotNetwork> obsoleteNetworks, Map<ChunkPos, BotNetwork> mutatedNetworks,
        Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> obsoleteJobAssignments, Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> mutatedJobAssignments)
        implements CustomPayload {
    public static final Id<BotSyncS2CPacket> ID = new Id<>(AutomataRegistry.packetId(AutomataRegistry.BOT_SYNC_S2C_PACKET));
    public static final PacketCodec<ByteBuf, BotSyncS2CPacket> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, BotSyncS2CPacket::clear,
            BotNetworkUtils.NETWORK_MAP_PACKET_CODEC, BotSyncS2CPacket::obsoleteNetworks, BotNetworkUtils.NETWORK_MAP_PACKET_CODEC, BotSyncS2CPacket::mutatedNetworks,
            BotJobUtils.JOB_ASSIGNMENT_PACKET_CODEC, BotSyncS2CPacket::obsoleteJobAssignments, BotJobUtils.JOB_ASSIGNMENT_PACKET_CODEC,
            BotSyncS2CPacket::mutatedJobAssignments, BotSyncS2CPacket::new);
    public static final BotSyncS2CPacket CLEAR = new BotSyncS2CPacket(true, Map.of(), Map.of(), Map.of(), Map.of());

    public BotSyncS2CPacket(boolean clear, Set<ServerBotNetwork> obsoleteNetworks, Set<ServerBotNetwork> mutatedNetworks,
            Set<BotJobAssignment> obsoleteJobAssignments, Set<BotJobAssignment> mutatedJobAssignments) {
        this(clear, BotNetworkUtils.map(obsoleteNetworks), BotNetworkUtils.map(mutatedNetworks), BotJobUtils.map(obsoleteJobAssignments),
                BotJobUtils.map(mutatedJobAssignments));
    }

    public BotSyncS2CPacket(Set<ServerBotNetwork> obsoleteNetworks, Set<ServerBotNetwork> mutatedNetworks, Set<BotJobAssignment> obsoleteJobAssignments,
            Set<BotJobAssignment> mutatedJobAssignments) {
        this(false, obsoleteNetworks, mutatedNetworks, obsoleteJobAssignments, mutatedJobAssignments);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
