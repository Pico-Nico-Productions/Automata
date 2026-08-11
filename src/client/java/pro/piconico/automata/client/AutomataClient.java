package pro.piconico.automata.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.EntityRendererFactories;
import pro.piconico.automata.client.network.handler.BotSyncS2CHandler;
import pro.piconico.automata.client.render.CommandToolRenderer;
import pro.piconico.automata.client.render.entity.ConstructionBotRenderer;
import pro.piconico.automata.client.screen.RoboportScreen;
import pro.piconico.automata.network.packet.BotSyncS2CPacket;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class AutomataClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BotSyncS2CPacket.ID, BotSyncS2CHandler::handle);

		CommandToolRenderer.initialize();
        EntityRendererFactories.register(AutomataEntities.CONSTRUCTION_BOT, ConstructionBotRenderer::new);
		
		HandledScreens.register(AutomataScreenHandlers.ROBOPORT, RoboportScreen::new);
	}
}