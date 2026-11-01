package pro.piconico.automata.registry;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.team.BotTeam;

public class AutomataTexts {
    public static final Style LINK_STYLE = Style.EMPTY.withColor(Formatting.AQUA).withUnderline(true);
    public static final Style AFFIRMATIVE = Style.EMPTY.withColor(Formatting.GREEN).withBold(true);
    public static final Style DISMISSIVE = Style.EMPTY.withColor(Formatting.RED).withBold(true);

    public static Text getCopyableText(String displayString, String value) {
        return Text.literal(displayString).setStyle(AutomataTexts.LINK_STYLE.withClickEvent(new ClickEvent.CopyToClipboard(displayString)));
    }

    public static Text getCopyableText(String string) {
        return getCopyableText(string, string);
    }

    /** Expected arguments: selection number and selection */
    public static final String BLOCK_SELECTED_KEY = AutomataRegistry.toTextTranslationKey("block_selected");

    public static MutableText getBlockSelected(int selectionNumber, BlockPos selection) {
        return Text.translatable(BLOCK_SELECTED_KEY, selectionNumber, selection.toShortString());
    }

    public static final String BOT_DEVICE_HOME_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_DEVICE + "_home");

    public static final MutableText getBotDeviceHome() {
        return Text.translatable(BOT_DEVICE_HOME_KEY);
    }

    /** Expected argument: job count */
    public static final String BOT_JOBS_ADDED_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_JOB + "s_added");

    public static final MutableText getBotJobsAdded(int jobCount) {
        return Text.translatable(BOT_JOBS_ADDED_KEY, jobCount);
    }

    /** Expected argument: job count */
    public static final String BOT_JOBS_REMOVED_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_JOB + "s_removed");

    public static final MutableText getBotJobsRemoved(int jobCount) {
        return Text.translatable(BOT_JOBS_REMOVED_KEY, jobCount);
    }

    public static final String BOT_TEAM_ADD_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_add");

    public static final MutableText getBotTeamAdd() {
        return Text.translatable(BOT_TEAM_ADD_KEY);
    }

    public static final String BOT_TEAM_ADD_MEMBER_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_add_member");

    public static final MutableText getBotTeamAddMember() {
        return Text.translatable(BOT_TEAM_ADD_MEMBER_KEY);
    }

    public static final String BOT_TEAM_CYCLE_ROLE_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_cycle_role");

    public static final MutableText getBotTeamCycleRole() {
        return Text.translatable(BOT_TEAM_CYCLE_ROLE_KEY);
    }

    public static final String BOT_TEAM_EMPTY_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_empty");

    public static final MutableText getBotTeamEmpty() {
        return Text.translatable(BOT_TEAM_EMPTY_KEY);
    }

    public static final String BOT_TEAM_MEMBERS_EMPTY_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_members_empty");

    public static final MutableText getBotTeamMembersEmpty() {
        return Text.translatable(BOT_TEAM_MEMBERS_EMPTY_KEY);
    }

    public static final String BOT_TEAM_MEMBERS_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_members");

    public static final MutableText getBotTeamMembers() {
        return Text.translatable(BOT_TEAM_MEMBERS_KEY);
    }

    public static final String BOT_TEAM_NON_MEMBERS_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_non_members");

    public static final MutableText getBotTeamNonMembers() {
        return Text.translatable(BOT_TEAM_NON_MEMBERS_KEY);
    }

    /** Expected arguments: player name, team name, and team uuid (truncated) */
    public static final String BOT_TEAM_PERMISSION_MISSING_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_permission_missing");

    public static final MutableText getBotTeamPermissionMissing(String playerName, BotTeam team) {
        return Text.translatable(BOT_TEAM_PERMISSION_MISSING_KEY, playerName, team.getName(), team.getTruncatedUuid());
    }

    public static final String BOT_TEAM_REMOVE_MEMBER_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_remove_member");

    public static final MutableText getBotTeamRemoveMember() {
        return Text.translatable(BOT_TEAM_REMOVE_MEMBER_KEY);
    }

    public static final String BOT_TEAM_SELECT_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_select");

    public static final MutableText getBotTeamSelect() {
        return Text.translatable(BOT_TEAM_SELECT_KEY);
    }

    public static final String BOT_TEAM_SETTINGS_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "_settings");

    public static final MutableText getBotTeamSettings() {
        return Text.translatable(BOT_TEAM_SETTINGS_KEY);
    }

    public static final String BOT_TEAMS_EMPTY_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_TEAM + "s_empty");

    public static final MutableText getBotTeamsEmpty() {
        return Text.translatable(BOT_TEAMS_EMPTY_KEY);
    }

    public static final String CREATE_KEY = AutomataRegistry.toTextTranslationKey("create");

    public static final MutableText getCreate() {
        return Text.translatable(CREATE_KEY);
    }

    public static final String DELETE_KEY = AutomataRegistry.toTextTranslationKey("delete");

    public static final MutableText getDelete() {
        return Text.translatable(DELETE_KEY);
    }

    public static final String DELIMITER_KEY = AutomataRegistry.toTextTranslationKey("delimiter");

    public static final MutableText getDelimiter() {
        return Text.translatable(DELIMITER_KEY);
    }

    public static final String MOUSE_LEFT_KEY = AutomataRegistry.toTextTranslationKey("mouse_left");

    public static final MutableText getMouseLeft() {
        return Text.translatable(MOUSE_LEFT_KEY);
    }

    public static final String MOUSE_RIGHT_KEY = AutomataRegistry.toTextTranslationKey("mouse_right");

    public static final MutableText getMouseRight() {
        return Text.translatable(MOUSE_RIGHT_KEY);
    }

    public static final String NAME_KEY = AutomataRegistry.toTextTranslationKey("name");

    public static final MutableText getName() {
        return Text.translatable(NAME_KEY);
    }

    /** Expected arguments: required selection count */
    public static final String SELECTION_INVALID = AutomataRegistry.toTextTranslationKey("selection_invalid");

    public static final MutableText getSelectionInvalid(int requiredSelectionCount) {
        return Text.translatable(SELECTION_INVALID, requiredSelectionCount);
    }

    public static final String UPDATE_KEY = AutomataRegistry.toTextTranslationKey("update");

    public static final MutableText getUpdate() {
        return Text.translatable(UPDATE_KEY);
    }

    public static void initialize() {
    }
}
