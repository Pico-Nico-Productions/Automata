package pro.piconico.automata.registry;

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

    /** Expected arguments: selection number and selection */
    public static final String BLOCK_SELECTED_KEY = AutomataRegistry.toTextTranslationKey("block_selected");

    public static MutableText getBlockSelected(int selectionNumber, BlockPos selection) {
        return Text.translatable(BLOCK_SELECTED_KEY, selectionNumber, selection.toShortString());
    }

    public static final String BOT_DEVICE_HOME_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_DEVICE + "_home");

    public static final MutableText getBotDeviceHome() {
        return Text.translatable(BOT_DEVICE_HOME_KEY);
    }

    public static final String BOT_DEVICE_TEAM_ADD_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_DEVICE + "_team_add");

    public static final MutableText getBotDeviceTeamAdd() {
        return Text.translatable(BOT_DEVICE_TEAM_ADD_KEY);
    }

    public static final String BOT_DEVICE_TEAM_SELECT_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_DEVICE + "_team_select");

    public static final MutableText getBotDeviceTeamSelect() {
        return Text.translatable(BOT_DEVICE_TEAM_SELECT_KEY);
    }

    public static final String BOT_DEVICE_TEAM_SETTINGS_KEY = AutomataRegistry.toTextTranslationKey(AutomataRegistry.BOT_DEVICE + "_team_settings");

    public static final MutableText getBotDeviceTeamSettings() {
        return Text.translatable(BOT_DEVICE_TEAM_SETTINGS_KEY);
    }

    public static final String CREATE_KEY = AutomataRegistry.toTextTranslationKey("create");

    public static final MutableText getCreate() {
        return Text.translatable(CREATE_KEY);
    }

    public static final String DECONSTRUCTION_FAILED_KEY = AutomataRegistry.toTextTranslationKey("deconstruction_failed");

    public static final MutableText getDeconstructionFailed() {
        return Text.translatable(DECONSTRUCTION_FAILED_KEY);
    }

    public static final String DELETE_KEY = AutomataRegistry.toTextTranslationKey("delete");

    public static final MutableText getDelete() {
        return Text.translatable(DELETE_KEY);
    }

    /** Expected argument: job count */
    public static final String JOBS_ADDED_KEY = AutomataRegistry.toTextTranslationKey("jobs_added");

    public static final MutableText getJobsAdded(int jobCount) {
        return Text.translatable(JOBS_ADDED_KEY, jobCount);
    }

    /** Expected argument: job count */
    public static final String JOBS_REMOVED_KEY = AutomataRegistry.toTextTranslationKey("jobs_removed");

    public static final MutableText getJobsRemoved(int jobCount) {
        return Text.translatable(JOBS_REMOVED_KEY, jobCount);
    }

    public static final String NAME_KEY = AutomataRegistry.toTextTranslationKey("name");

    public static final MutableText getName() {
        return Text.translatable(NAME_KEY);
    }

    public static final String TEAM_EMPTY_KEY = AutomataRegistry.toTextTranslationKey("team_empty");

    public static final MutableText getTeamEmpty() {
        return Text.translatable(TEAM_EMPTY_KEY);
    }

    public static final String TEAMS_EMPTY_KEY = AutomataRegistry.toTextTranslationKey("teams_empty");

    public static final MutableText getTeamsEmpty() {
        return Text.translatable(TEAMS_EMPTY_KEY);
    }

    public static final String UPDATE_KEY = AutomataRegistry.toTextTranslationKey("update");

    public static final MutableText getUpdate() {
        return Text.translatable(UPDATE_KEY);
    }

    public static MutableText format(String format, Object... args) {
        return Text.literal(String.format(format, args));
    }

    public static void initialize() {
    }
}
