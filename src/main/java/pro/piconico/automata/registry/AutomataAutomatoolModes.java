package pro.piconico.automata.registry;

import java.util.Optional;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.function.TriFunction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pro.piconico.automata.bot.mode.AutomatoolMode;
import pro.piconico.automata.bot.mode.BlueprintAutomatoolMode;
import pro.piconico.automata.bot.mode.ConstructionAutomatoolMode;
import pro.piconico.automata.bot.mode.DeconstructionAutomatoolMode;
import pro.piconico.automata.component.SelectionComponent.Selection;

public class AutomataAutomatoolModes {
    public static final AutomatoolMode BLUEPRINT = register(AutomataRegistry.BLUEPRINT, false, Selection.TWO, BlueprintAutomatoolMode::dispatch);
    public static final AutomatoolMode CONSTRUCTION = register(AutomataRegistry.CONSTRUCTION, true, Selection.ONE, ConstructionAutomatoolMode::canDispatch,
            ConstructionAutomatoolMode::dispatch);
    public static final AutomatoolMode DECONSTRUCTION = register(AutomataRegistry.DECONSTRUCTION, true, Selection.TWO, DeconstructionAutomatoolMode::dispatch);

    private static AutomatoolMode register(String name, boolean requiresTeam, Optional<Selection> maxSelections,
            Optional<TriFunction<PlayerEntity, ItemStack, Boolean, Boolean>> canDispatch, BiConsumer<ServerPlayerEntity, ItemStack> dispatch) {
        Identifier identifier = AutomataRegistry.id(name);
        AutomatoolMode mode = new AutomatoolMode(requiresTeam, maxSelections, canDispatch, dispatch);
        Registry.register(AutomataRegistries.AUTOMATOOL_MODE, identifier, mode);

        return mode;
    }

    private static AutomatoolMode register(String name, boolean requiresTeam, Selection maxSelections,
            TriFunction<PlayerEntity, ItemStack, Boolean, Boolean> canDispatch, BiConsumer<ServerPlayerEntity, ItemStack> dispatch) {
        return register(name, requiresTeam, Optional.of(maxSelections), Optional.of(canDispatch), dispatch);
    }

    private static AutomatoolMode register(String name, boolean requiresTeam, Selection maxSelections, BiConsumer<ServerPlayerEntity, ItemStack> dispatch) {
        return register(name, requiresTeam, Optional.of(maxSelections), Optional.empty(), dispatch);
    }

    public static void initialize() {
    }
}
