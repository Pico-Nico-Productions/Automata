package pro.piconico.automata.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.EntityRendererFactories;
import pro.piconico.automata.client.event.CommandToolClientEvents;
import pro.piconico.automata.client.render.CommandToolRenderer;
import pro.piconico.automata.client.render.entity.ConstructionBotRenderer;
import pro.piconico.automata.client.screen.RoboportScreen;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class AutomataClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        CommandToolClientEvents.initialize();

		CommandToolRenderer.initialize();
        EntityRendererFactories.register(AutomataEntities.CONSTRUCTION_BOT, ConstructionBotRenderer::new);
		
		HandledScreens.register(AutomataScreenHandlers.ROBOPORT, RoboportScreen::new);
	}
}