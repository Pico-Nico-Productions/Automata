package pro.piconico.automata.client.registry;

import io.wispforest.owo.ui.core.Color;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.DeconstructionBotJob;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamRole;

public class AutomataClientColors {
    public static final Color SELECTION_BOUNDS = Color.ofRgb(0x00FFFF); // Cyan
    public static final Color SELECTION1 = Color.GREEN;
    public static final Color SELECTION2 = Color.BLUE;

    public static final Color NETWORK = Color.ofArgb(0x7FFF7FFF); // Pink
    public static final Color DECONSTRUCTION = Color.RED;
    public static final Color JOB_DEFAULT = Color.WHITE;

    public static Color getJobColor(BotJob botJob) {
        return switch (botJob) {
        case DeconstructionBotJob ignored -> DECONSTRUCTION;
        default -> JOB_DEFAULT;
        };
    }

    public static final Color USER = Color.WHITE;
    public static final Color ADMIN = Color.BLUE;
    public static final Color OWNER = Color.ofArgb(0x7FFF7FFF); // Pink

    public static Color getRoleColor(BotTeamRole role) {
        return switch (role) {
        case USER -> USER;
        case ADMIN -> ADMIN;
        case OWNER -> OWNER;
        };
    }

    public static final Color BUTTON_INACTIVE = Color.ofArgb(0x44000000); // Black
    public static final Color BUTTON_ACTIVE = Color.ofRgb(0x444444); // Dark gray
    public static final Color BUTTON_HOVERED = Color.ofRgb(0x888888); // Gray
    public static final Color BUTTON_OUTLINE = Color.ofRgb(0xCCCCCC); // Light gray
    public static final Color BUTTON_SELECTED = Color.WHITE;

    public static final Color RESERVATION = Color.BLUE;
}
