package pro.piconico.automata.registry;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;

public class AutomataPackets {
    public static final CustomPayload.Type<?, BotSyncS2CPacket> BOT_SYNC_S2C_PACKET = registerS2C(BotSyncS2CPacket.ID, BotSyncS2CPacket.PACKET_CODEC);

    private static <T extends CustomPayload> CustomPayload.Type<?, T> registerS2C(CustomPayload.Id<T> id, PacketCodec<ByteBuf, T> codec) {
        return PayloadTypeRegistry.playS2C().register(id, codec);
    }

    public static void initialize() {
    }
}