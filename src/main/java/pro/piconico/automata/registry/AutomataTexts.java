package pro.piconico.automata.registry;

import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class AutomataTexts {
    public static final Style LINK_STYLE = Style.EMPTY.withColor(Formatting.AQUA).withUnderline(true);

    /** Expected arguments: selection number and selection */
    public static final String BLOCK_SELECTED = AutomataRegistry.toTextTranslationKey("block_selected");
    public static final String BOT_DEVICE_HOME = AutomataRegistry.toTextTranslationKey("bot_device_home");
    public static final String BOT_DEVICE_TEAM_SELECT = AutomataRegistry.toTextTranslationKey("bot_device_team_select");
    public static final String DECONSTRUCTION_FAILED = AutomataRegistry.toTextTranslationKey("deconstruction_failed");
    /** Expected argument: job count */
    public static final String JOBS_ADDED = AutomataRegistry.toTextTranslationKey("jobs_added");
    /** Expected argument: job count */
    public static final String JOBS_REMOVED = AutomataRegistry.toTextTranslationKey("jobs_removed");
    /** Expected arguments: team name and team uuid */
    public static final String TEAM = AutomataRegistry.toTextTranslationKey("team");
    /** Expected arguments: team name and team uuid */
    public static final String TEAM_ADDED = AutomataRegistry.toTextTranslationKey("team_added");
    /** Expected arguments: team name and team uuid */
    public static final String TEAM_REMOVED = AutomataRegistry.toTextTranslationKey("team_removed");
    /** Expected argument: team uuid */
    public static final String TEAM_MISSING = AutomataRegistry.toTextTranslationKey("team_missing");
    /** Expected argument: team name */
    public static final String TEAM_NAME_INVALID = AutomataRegistry.toTextTranslationKey("team_name_invalid");
    /** Expected argument: team name */
    public static final String TEAM_SELECTED = AutomataRegistry.toTextTranslationKey("team_selected");
    public static final String TEAMS = AutomataRegistry.toTextTranslationKey("teams");
    public static final String TEAMS_EMPTY = AutomataRegistry.toTextTranslationKey("teams_empty");

    public static void initialize() {
    }
}
