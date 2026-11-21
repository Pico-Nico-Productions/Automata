package pro.piconico.automata.bot.mode;

import java.util.Optional;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.function.TriFunction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.component.BotTeamComponent;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.component.SelectionComponent.Selection;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.registry.AutomataRegistries;
import pro.piconico.automata.registry.AutomataRegistry;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotTeamPersistentState;

public record AutomatoolMode(boolean requiresTeam, Optional<Selection> maxSelections,
        Optional<TriFunction<PlayerEntity, ItemStack, Boolean, Boolean>> canDispatch, BiConsumer<ServerPlayerEntity, ItemStack> dispatch) {
    public boolean requiresSelection() {
        return maxSelections.isPresent();
    }

    public int maxSelectionsInt() {
        return maxSelections.map(max -> max.toInt()).orElse(0);
    }

    public boolean canDispatch(PlayerEntity player, ItemStack stack, boolean sendFailMessage) {
        return canDispatch.map(can -> can.apply(player, stack, sendFailMessage)).orElse(canDispatch(this, player, stack, sendFailMessage));
    }

    public RegistryKey<AutomatoolMode> getRegistryKey() {
        return AutomataRegistries.AUTOMATOOL_MODE.getKey(this).get();
    }

    public String getTranslationKey() {
        return AutomataRegistry.toAutomatoolModeTranslationKey(getRegistryKey().getValue().getPath());
    }

    public MutableText getName() {
        return Text.translatable(getTranslationKey());
    }

    public static boolean canDispatch(AutomatoolMode mode, PlayerEntity player, ItemStack stack, boolean sendFailMessage) {
        if (!stack.isOf(AutomataItems.AUTOMATOOL))
            return false;

        boolean isClient = player.getEntityWorld().isClient();

        if (mode.requiresTeam && BotTeamComponent.get(stack).filter(uuid -> isClient //
                || BotTeamPersistentState.teamExistsAndHasMemberWith(uuid, (ServerPlayerEntity)player, BotTeamPermission.DISPATCH)).isEmpty()) {
            if (sendFailMessage) {
                player.sendMessage(AutomataTexts.getBotTeamEmpty(), true);
            }
            return false;
        }

        if (mode.requiresSelection() && SelectionComponent.get(stack).filter(selection -> (isClient || selection.getWorld(player).isPresent()) //
                && selection.getSelectionCount() > 0).isEmpty()) {
            if (sendFailMessage) {
                player.sendMessage(AutomataTexts.getSelectionInvalid(), true);
            }
            return false;
        }

        return true;
    }
}
