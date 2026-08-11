package pro.piconico.automata.network.packet;

import java.util.Map;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.job.BotJobUtils;
import pro.piconico.automata.registry.AutomataRegistry;

public record BotSyncS2CPacket(boolean clear, Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> staleJobAssignments,
        Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> newJobAssignments) implements CustomPayload {
    public static final Id<BotSyncS2CPacket> ID = new Id<>(AutomataRegistry.packetId(AutomataRegistry.BOT_SYNC_S2C_PACKET));
    public static final PacketCodec<ByteBuf, BotSyncS2CPacket> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, BotSyncS2CPacket::clear,
            BotJobUtils.JOB_ASSIGNMENT_PACKET_CODEC, BotSyncS2CPacket::staleJobAssignments, BotJobUtils.JOB_ASSIGNMENT_PACKET_CODEC,
            BotSyncS2CPacket::newJobAssignments, BotSyncS2CPacket::new);
    public static final BotSyncS2CPacket CLEAR = new BotSyncS2CPacket(true, Map.of(), Map.of());

    public BotSyncS2CPacket(Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> staleJobAssignments,
            Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> newJobAssignments) {
        this(false, staleJobAssignments, newJobAssignments);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
