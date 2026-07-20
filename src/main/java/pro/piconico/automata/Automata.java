package pro.piconico.automata;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.piconico.automata.bot.BotDispatcher;
import pro.piconico.automata.registry.AutomataBlocks;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.registry.AutomataPersistentStates;
import pro.piconico.automata.registry.AutomataScreenHandlers;
import pro.piconico.automata.registry.AutomataTexts;

public class Automata implements ModInitializer {
	public static final String MOD_ID = "automata";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		AutomataTexts.initialize();
		AutomataComponents.initialize();
		AutomataEntities.initialize();
		AutomataItems.initialize();
		AutomataBlocks.initialize();
		AutomataPersistentStates.initialize();
        BotDispatcher.initialize();
		AutomataScreenHandlers.initialize();

		LOGGER.info("Automata Initialized!");
	}
}
