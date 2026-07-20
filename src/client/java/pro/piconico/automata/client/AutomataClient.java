package pro.piconico.automata.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import pro.piconico.automata.client.event.CommandToolClientEvents;
import pro.piconico.automata.client.network.handler.BotCacheS2CHandler;
import pro.piconico.automata.client.render.CommandToolRenderer;
import pro.piconico.automata.client.screen.RoboportScreen;
import pro.piconico.automata.network.packet.BotCacheS2CPacket;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class AutomataClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BotCacheS2CPacket.ID, BotCacheS2CHandler::handle);

        CommandToolClientEvents.initialize();

		CommandToolRenderer.initialize();
		
		HandledScreens.register(AutomataScreenHandlers.ROBOPORT, RoboportScreen::new);
	}
}