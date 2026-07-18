package pro.piconico.automata.registry;

import pro.piconico.automata.Automata;

public class AutomataTexts {
    /** Expected arguments: selection number and selection */
    public static final String SELECTION = toTranslationKey("selection");
    public static final String INVALID_DECONSTRUCTION_SELECTION = toTranslationKey("invalid_deconstruction_selection");
    /** Expected arguments: selection 1 and selection 2 */
    public static final String DECONSTRUCTION = toTranslationKey("deconstruction");

    private static String toTranslationKey(String name) {
        return "text." + Automata.MOD_ID + "." + name;
    }

    public static void initialize() {

    }
}
