package pro.piconico.automata.client.registry;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import pro.piconico.automata.client.network.handler.BotSyncS2CHandler;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;

public class AutomataClientPacketHandlers {
    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(BotSyncS2CPacket.ID, BotSyncS2CHandler::handle);
    }
}
