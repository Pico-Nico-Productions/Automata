package pro.piconico.automata.client;

import net.fabricmc.api.ClientModInitializer;
import pro.piconico.automata.client.registry.AutomataClientMessageHandlers;
import pro.piconico.automata.client.registry.AutomataClientPacketHandlers;
import pro.piconico.automata.client.registry.AutomataClientRenderPipelines;
import pro.piconico.automata.client.registry.AutomataClientRenderers;
import pro.piconico.automata.client.registry.AutomataClientScreens;
import pro.piconico.automata.client.registry.AutomataClientTextures;

public class AutomataClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        AutomataClientPacketHandlers.initialize();
        AutomataClientMessageHandlers.initialize();
        AutomataClientRenderPipelines.initialize();
        AutomataClientTextures.initialize();
        AutomataClientRenderers.initialize();
        AutomataClientScreens.initialize();
	}
}