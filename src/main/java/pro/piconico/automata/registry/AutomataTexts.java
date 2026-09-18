package pro.piconico.automata.registry;

import java.util.UUID;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class AutomataTexts {
    public static final Style LINK_STYLE = Style.EMPTY.withColor(Formatting.AQUA).withUnderline(true);

    public static Text getCopyableText(String string) {
        return Text.literal(string).setStyle(AutomataTexts.LINK_STYLE.withClickEvent(new ClickEvent.CopyToClipboard(string)));
    }

    //#region Vanilla
    public static final MutableText NONE = Text.translatable("gui.none");
    //#endregion

    /** Expected arguments: selection number and selection */
    public static final String BLOCK_SELECTED_KEY = AutomataRegistry.toTextTranslationKey("block_selected");

    public static MutableText getBlockSelected(int selectionNumber, BlockPos selection) {
        return Text.translatable(BLOCK_SELECTED_KEY, selectionNumber, selection.toShortString());
    }

    public static final String BOT_DEVICE_HOME_KEY = AutomataRegistry.toTextTranslationKey("bot_device_home");

    public static final MutableText getBotDeviceHome() {
        return Text.translatable(BOT_DEVICE_HOME_KEY);
    }

    public static final String BOT_DEVICE_TEAM_SELECT_KEY = AutomataRegistry.toTextTranslationKey("bot_device_team_select");

    public static final MutableText getBotDeviceTeamSelect() {
        return Text.translatable(BOT_DEVICE_TEAM_SELECT_KEY);
    }

    public static final String DECONSTRUCTION_FAILED_KEY = AutomataRegistry.toTextTranslationKey("deconstruction_failed");

    public static final MutableText getDeconstructionFailed() {
        return Text.translatable(DECONSTRUCTION_FAILED_KEY);
    }

    /** Expected argument: job count */
    public static final String JOBS_ADDED_KEY = AutomataRegistry.toTextTranslationKey("jobs_added");

    public static MutableText getJobsAdded(int jobCount) {
        return Text.translatable(JOBS_ADDED_KEY, jobCount);
    }

    /** Expected argument: job count */
    public static final String JOBS_REMOVED_KEY = AutomataRegistry.toTextTranslationKey("jobs_removed");

    public static MutableText getJobsRemoved(int jobCount) {
        return Text.translatable(JOBS_REMOVED_KEY, jobCount);
    }

    /** Expected arguments: team name and team uuid */
    public static final String TEAM_KEY = AutomataRegistry.toTextTranslationKey("team");

    public static MutableText getTeam(String teamName, UUID teamUuid) {
        return Text.translatable(TEAM_KEY, teamName, getCopyableText(teamUuid.toString()));
    }

    /** Expected arguments: team name and team uuid */
    public static final String TEAM_ADDED_KEY = AutomataRegistry.toTextTranslationKey("team_added");

    public static MutableText getTeamAdded(String teamName, UUID teamUuid) {
        return Text.translatable(TEAM_ADDED_KEY, teamName, getCopyableText(teamUuid.toString()));
    }

    /** Expected arguments: team name and team uuid */
    public static final String TEAM_REMOVED_KEY = AutomataRegistry.toTextTranslationKey("team_removed");

    public static MutableText getTeamRemoved(String teamName, UUID teamUuid) {
        return Text.translatable(TEAM_REMOVED_KEY, teamName, getCopyableText(teamUuid.toString()));
    }

    /** Expected argument: team uuid */
    public static final String TEAM_MISSING_KEY = AutomataRegistry.toTextTranslationKey("team_missing");

    public static MutableText getTeamMissing(String teamUuid) {
        return Text.translatable(TEAM_MISSING_KEY, getCopyableText(teamUuid.toString()));
    }

    /** Expected argument: team name */
    public static final String TEAM_NAME_INVALID_KEY = AutomataRegistry.toTextTranslationKey("team_name_invalid");

    public static MutableText getTeamNameInvalid(String teamName) {
        return Text.translatable(TEAM_NAME_INVALID_KEY, teamName);
    }

    /** Expected argument: team name */
    public static final String TEAM_SELECTED_KEY = AutomataRegistry.toTextTranslationKey("team_selected");

    public static MutableText getTeamSelected(String teamName) {
        return Text.translatable(TEAM_SELECTED_KEY, teamName);
    }

    public static final String TEAMS_KEY = AutomataRegistry.toTextTranslationKey("teams");

    public static final MutableText getTeams() {
        return Text.translatable(TEAMS_KEY);
    }

    public static final String TEAMS_EMPTY_KEY = AutomataRegistry.toTextTranslationKey("teams_empty");

    public static final MutableText getTeamsEmpty() {
        return Text.translatable(TEAMS_EMPTY_KEY);
    }

    public static void initialize() {
    }
}
