package pro.piconico.automata.registry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.function.TriFunction;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.DeconstructionBotJob;
import pro.piconico.automata.bot.mode.AutomatoolMode;
import pro.piconico.automata.component.SelectionComponent.Selection;

public class AutomataAutomatoolModes {
    public static final AutomatoolMode DECONSTRUCTION = register(AutomataRegistry.DECONSTRUCTION, Selection.TWO, DeconstructionBotJob::dispatch);

    private static AutomatoolMode register(String name, Optional<Selection> selectionRequirement,
            TriFunction<ServerWorld, UUID, List<BlockPos>, Text> dispatch) {
        Identifier identifier = AutomataRegistry.id(name);
        AutomatoolMode mode = Registry.register(AutomataRegistries.AUTOMATOOL_MODE, identifier, new AutomatoolMode(selectionRequirement, dispatch));

        return mode;
    }

    private static AutomatoolMode register(String name, Selection selectionRequirement,
            TriFunction<ServerWorld, UUID, List<BlockPos>, Text> dispatch) {
        return register(name, Optional.of(selectionRequirement), dispatch);
    }

    public static void initialize() {
    }
}
