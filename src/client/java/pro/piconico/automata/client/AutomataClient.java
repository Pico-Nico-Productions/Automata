package pro.piconico.automata.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import pro.piconico.automata.client.screen.RoboportScreen;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class AutomataClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(AutomataScreenHandlers.ROBOPORT, RoboportScreen::new);
	}
}