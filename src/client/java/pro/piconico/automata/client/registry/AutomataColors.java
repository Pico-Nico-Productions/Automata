package pro.piconico.automata.client.registry;

import java.util.Optional;
import io.wispforest.owo.ui.core.Color;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.DeconstructionBotJob;

public class AutomataColors {
    public static final Color SELECTION_BOUNDS = Color.ofRgb(0x00FFFF); // Cyan
    public static final Color SELECTION1 = Color.ofRgb(0x00FF00); // Green
    public static final Color SELECTION2 = Color.ofRgb(0x0000FF); // Blue

    public static final Color NETWORK = Color.ofArgb(0x7FFF7FFF); // Pink
    public static final Color DECONSTRUCTION = Color.ofRgb(0xFF0000); // Red
    public static Optional<Color> getJobColor(BotJob botJob) {
        return switch (botJob) {
        case DeconstructionBotJob ignored -> Optional.of(DECONSTRUCTION);
        default -> Optional.empty();
        };
    }

    public static final Color ACTIVE = Color.ofRgb(0xFFFFFFFF); // White
    public static final Color HOVERED = Color.ofRgb(0xFF999999); // Light gray
    public static final Color INACTIVE = Color.ofRgb(0xFF666666); // Gray
}
