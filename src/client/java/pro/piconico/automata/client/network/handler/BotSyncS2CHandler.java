package pro.piconico.automata.client.network.handler;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import pro.piconico.automata.client.network.BotCache;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;

public class BotSyncS2CHandler {
    public static void handle(BotSyncS2CPacket payload, ClientPlayNetworking.Context context) {
        if (payload.clear()) {
            BotCache.clear();
        }
        
        BotCache.applyDelta(payload.deltaNetworkMap());

        BotCache.applyDelta(payload.deltaJobAssignmentMap());
    }
}
