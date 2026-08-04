package pro.piconico.automata.network.packet;

import java.util.Map;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.job.BotJobUtils;
import pro.piconico.automata.registry.AutomataRegistry;

public record BotSyncS2CPacket(Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> newJobAssignments,
        Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> staleJobAssignments) implements CustomPayload {
    public static final Id<BotSyncS2CPacket> ID = new Id<>(AutomataRegistry.packetId(AutomataRegistry.BOT_SYNC_S2C_PACKET));
    public static final PacketCodec<ByteBuf, BotSyncS2CPacket> PACKET_CODEC = PacketCodec.tuple(BotJobUtils.JOB_ASSIGNMENT_PACKET_CODEC,
            BotSyncS2CPacket::newJobAssignments, BotJobUtils.JOB_ASSIGNMENT_PACKET_CODEC, BotSyncS2CPacket::staleJobAssignments, BotSyncS2CPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
