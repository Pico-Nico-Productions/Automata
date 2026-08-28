package pro.piconico.automata.client.registry;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import pro.piconico.automata.client.screen.RoboportScreen;
import pro.piconico.automata.registry.AutomataScreenHandlers;

public class AutomataClientScreens {
    public static void initialize() {
        HandledScreens.register(AutomataScreenHandlers.ROBOPORT, RoboportScreen::new);
    }
}
