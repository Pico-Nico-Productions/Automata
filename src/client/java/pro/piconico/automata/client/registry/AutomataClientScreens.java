package pro.piconico.automata.client.registry;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import pro.piconico.automata.client.gui.screen.AutomatoolScreen;
import pro.piconico.automata.client.gui.screen.RoboportScreen;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class AutomataClientScreens {
    public static void initialize() {
        HandledScreens.register(AutomataScreenHandlers.AUTOMATOOL, AutomatoolScreen::new);
        HandledScreens.register(AutomataScreenHandlers.ROBOPORT, RoboportScreen::new);
    }
}
