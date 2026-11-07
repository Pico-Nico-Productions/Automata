package pro.piconico.automata.registry;

import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.function.TriFunction;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pro.piconico.automata.bot.mode.AutomatoolMode;
import pro.piconico.automata.bot.mode.BlueprintAutomatoolMode;
import pro.piconico.automata.bot.mode.DeconstructionAutomatoolMode;
import pro.piconico.automata.component.SelectionComponent;

public class AutomataAutomatoolModes {
    public static final AutomatoolMode BLUEPRINT = register(AutomataRegistry.BLUEPRINT, false, true, BlueprintAutomatoolMode::dispatch);
    public static final AutomatoolMode DECONSTRUCTION = register(AutomataRegistry.DECONSTRUCTION, true, true, DeconstructionAutomatoolMode::dispatch);

    private static AutomatoolMode register(String name, boolean requiresTeam, boolean requiresSelection,
            TriFunction<ServerPlayerEntity, Optional<UUID>, Optional<SelectionComponent>, Optional<Text>> dispatch) {
        Identifier identifier = AutomataRegistry.id(name);
        AutomatoolMode mode = Registry.register(AutomataRegistries.AUTOMATOOL_MODE, identifier, new AutomatoolMode(requiresTeam, requiresSelection, dispatch));

        return mode;
    }

    public static void initialize() {
    }
}
