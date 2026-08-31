package pro.piconico.automata.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import pro.piconico.automata.bot.job.BotJobAssignmentMap;
import pro.piconico.automata.bot.network.BotNetworkMap;
import pro.piconico.automata.registry.AutomataRegistry;

public record BotSyncS2CPacket(boolean clear, BotNetworkMap<?> deltaNetworkMap, BotJobAssignmentMap deltaJobAssignmentMap) implements CustomPayload {
    public static final Id<BotSyncS2CPacket> ID = new Id<>(AutomataRegistry.packetId(AutomataRegistry.BOT_SYNC_S2C_PACKET));
    public static final PacketCodec<ByteBuf, BotSyncS2CPacket> PACKET_CODEC = PacketCodec.tuple( //
            PacketCodecs.BOOLEAN, BotSyncS2CPacket::clear, //
            BotNetworkMap.PACKET_CODEC, BotSyncS2CPacket::deltaNetworkMap, //
            BotJobAssignmentMap.PACKET_CODEC, BotSyncS2CPacket::deltaJobAssignmentMap, //
            BotSyncS2CPacket::new);
    public static final BotSyncS2CPacket CLEAR = new BotSyncS2CPacket(true, new BotNetworkMap<>(), new BotJobAssignmentMap());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
