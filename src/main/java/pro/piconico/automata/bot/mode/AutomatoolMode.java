package pro.piconico.automata.bot.mode;

import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.function.TriFunction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.registry.AutomataRegistries;
import pro.piconico.automata.registry.AutomataRegistry;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotTeamPersistentState;

public record AutomatoolMode(boolean requiresTeam, boolean requiresSelection,
        TriFunction<ServerPlayerEntity, Optional<UUID>, Optional<SelectionComponent>, Optional<Text>> dispatch) {
    public RegistryKey<AutomatoolMode> getRegistryKey() {
        return AutomataRegistries.AUTOMATOOL_MODE.getKey(this).get();
    }

    public String getTranslationKey() {
        return AutomataRegistry.toAutomatoolModeTranslationKey(getRegistryKey().getValue().getPath());
    }

    public MutableText getName() {
        return Text.translatable(getTranslationKey());
    }

    public static Optional<Text> getRequirementFailure(AutomatoolMode mode, PlayerEntity player, Optional<UUID> teamUuid,
            Optional<SelectionComponent> selectionComponent) {
        boolean isClient = player.getEntityWorld().isClient();

        if (mode.requiresTeam && teamUuid.filter(uuid -> isClient //
                || BotTeamPersistentState.teamExistsAndHasMemberWith(uuid, (ServerPlayerEntity)player, BotTeamPermission.DISPATCH)).isEmpty())
            return Optional.of(AutomataTexts.getBotTeamEmpty());

        if (mode.requiresSelection && selectionComponent.filter(selection -> (isClient || selection.getWorld(player).isPresent()) //
                && selection.getSelectionCount() > 0).isEmpty())
            return Optional.of(AutomataTexts.getSelectionInvalid());

        return Optional.empty();
    }
}
