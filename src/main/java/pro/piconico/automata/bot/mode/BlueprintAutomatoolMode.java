package pro.piconico.automata.bot.mode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.item.BlueprintItem;
import pro.piconico.automata.registry.AutomataAutomatoolModes;
import pro.piconico.automata.registry.AutomataTexts;

public class BlueprintAutomatoolMode {
    public static Optional<Text> dispatch(ServerPlayerEntity serverPlayer, UUID teamUuid, ServerWorld serverWorld, List<BlockPos> selections) {
        if (selections.isEmpty())
            return Optional.of(AutomataTexts.getSelectionInvalid());

        serverPlayer.giveOrDropStack(BlueprintItem.create(serverWorld, selections));

        return Optional.empty();
    }

    public static Optional<Text> dispatch(ServerPlayerEntity serverPlayer, Optional<UUID> teamUuid, Optional<SelectionComponent> selection) {
        Optional<Text> requirementFailure = AutomatoolMode.getRequirementFailure(AutomataAutomatoolModes.BLUEPRINT, serverPlayer, teamUuid, selection);
        if (requirementFailure.isPresent())
            return requirementFailure;

        return dispatch(serverPlayer, teamUuid.get(), selection.get().getWorld(serverPlayer).get(), selection.get().getSelections());
    }
}
