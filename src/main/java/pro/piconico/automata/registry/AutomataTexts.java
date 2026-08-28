package pro.piconico.automata.registry;

import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import pro.piconico.automata.Automata;

public class AutomataTexts {
    public static final Style LINK_STYLE = Style.EMPTY.withColor(Formatting.AQUA).withUnderline(true);

    /** Expected arguments: selection number and selection */
    public static final String BLOCK_SELECTED = toTranslationKey("block_selected");
    public static final String DECONSTRUCTION_FAILED = toTranslationKey("deconstruction_failed");
    /** Expected argument: job count */
    public static final String JOBS_ADDED = toTranslationKey("jobs_added");
    /** Expected argument: job count */
    public static final String JOBS_REMOVED = toTranslationKey("jobs_removed");
    /** Expected arguments: team name and team uuid */
    public static final String TEAM = toTranslationKey("team");
    /** Expected arguments: team name and team uuid */
    public static final String TEAM_CREATED = toTranslationKey("team_created");
    /** Expected arguments: team name and team uuid */
    public static final String TEAM_DELETED = toTranslationKey("team_deleted");
    /** Expected argument: team uuid */
    public static final String TEAM_MISSING = toTranslationKey("team_missing");
    /** Expected argument: team name */
    public static final String TEAM_NAME_INVALID = toTranslationKey("team_name_invalid");
    /** Expected argument: team name */
    public static final String TEAM_SELECTED = toTranslationKey("team_selected");
    public static final String TEAMS_EMPTY = toTranslationKey("teams_empty");

    private static String toTranslationKey(String name) {
        return "text." + Automata.MOD_ID + "." + name;
    }

    public static void initialize() {

    }
}
