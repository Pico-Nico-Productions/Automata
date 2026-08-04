package pro.piconico.automata;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.piconico.automata.registry.AutomataBlocks;
import pro.piconico.automata.registry.AutomataCommands;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataEntities;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.registry.AutomataNetworkManagers;
import pro.piconico.automata.registry.AutomataPackets;
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
        AutomataPackets.initialize();
        AutomataNetworkManagers.initialize();
		AutomataScreenHandlers.initialize();
        AutomataCommands.initialize();

		LOGGER.info("Automata Initialized!");
	}

    public static void logError(String message, Function<String, ? extends RuntimeException> errorFactory) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            throw errorFactory.apply(message);
        }
        else {
            LOGGER.error(message);
        }
    }
}
