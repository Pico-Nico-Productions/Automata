package pro.piconico.automata.network.packet;

import java.util.List;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.registry.AutomataRegistry;

public record BotCacheS2CPacket(
    List<BlockPos> roboports
) implements CustomPayload {
    public static final Id<BotCacheS2CPacket> ID = new Id<>(AutomataRegistry.packetId(AutomataRegistry.BOT_CACHE_PACKET));
    public static final PacketCodec<ByteBuf, BotCacheS2CPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC.collect(PacketCodecs.toList()), BotCacheS2CPacket::roboports,
        BotCacheS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
