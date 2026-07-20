package pro.piconico.automata.client.network.handler;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import pro.piconico.automata.client.cache.BotCache;
import pro.piconico.automata.network.packet.BotCacheS2CPacket;

public class BotCacheS2CHandler {
    public static void handle(BotCacheS2CPacket payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            BotCache.ROBOPORTS.clear();
            BotCache.ROBOPORTS.addAll(payload.roboports());
        });
    }
}
