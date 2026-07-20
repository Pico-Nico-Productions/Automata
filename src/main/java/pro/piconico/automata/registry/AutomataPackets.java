package pro.piconico.automata.registry;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import pro.piconico.automata.network.packet.BotCacheS2CPacket;
import pro.piconico.automata.network.sync.BotCacheSynchronizer;

public class AutomataPackets {
    public static final CustomPayload.Type<?, BotCacheS2CPacket> BOT_CACHE_S2C_PACKET = register(BotCacheS2CPacket.ID, BotCacheS2CPacket.CODEC);

    private static <T extends CustomPayload> CustomPayload.Type<?, T> register(CustomPayload.Id<T> id, PacketCodec<ByteBuf, T> codec) {
        return PayloadTypeRegistry.playS2C().register(id, codec);
    }

    public static void initialize() {
        BotCacheSynchronizer.initialize();
    }
}
