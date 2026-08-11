package pro.piconico.automata.registry;

import pro.piconico.automata.Automata;

public class AutomataTexts {
    public static final String DECONSTRUCTION_FAILED = toTranslationKey("deconstruction_failed");
    /** Expected arguments: network count */
    public static final String NETWORKS_REMOVED = toTranslationKey("networks_removed");
    /** Expected arguments: job count */
    public static final String JOBS_ADDED = toTranslationKey("jobs_added");
    /** Expected arguments: job count */
    public static final String JOBS_REMOVED = toTranslationKey("jobs_removed");
    /** Expected arguments: selection number and selection */
    public static final String SELECTED = toTranslationKey("selected");

    private static String toTranslationKey(String name) {
        return "text." + Automata.MOD_ID + "." + name;
    }

    public static void initialize() {

    }
}
