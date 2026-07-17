package pro.piconico.automata;

import net.fabricmc.api.ModInitializer;

import pro.piconico.automata.registry.AutomataBlocks;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.registry.AutomataScreenHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Automata implements ModInitializer {
	public static final String MOD_ID = "automata";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		AutomataItems.initialize();
		AutomataBlocks.initialize();
		AutomataEntities.initialize();
		AutomataScreenHandlers.initialize();

		LOGGER.info("Automata Initialized!");
	}
}
