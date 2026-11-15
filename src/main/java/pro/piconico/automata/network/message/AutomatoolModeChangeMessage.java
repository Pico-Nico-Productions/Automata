package pro.piconico.automata.network.message;

import java.util.Optional;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.bot.device.ItemBotDevice.ItemId;
import pro.piconico.automata.bot.mode.AutomatoolMode;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.component.AutomatoolModeComponent;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataRegistries;
import pro.piconico.automata.world.BotTeamPersistentState;

public record AutomatoolModeChangeMessage(int slot, Identifier modeId) {
    public AutomatoolModeChangeMessage(int slot, AutomatoolMode mode) {
        this(slot, mode.getRegistryKey().getValue());
    }

    public static void handle(AutomatoolModeChangeMessage message, ServerAccess serverAccess) {
        ServerPlayerEntity serverPlayer = serverAccess.player();
        Optional<ItemBotDevice> automatool = ItemBotDevice.resolve(serverPlayer, new ItemId(message.slot));

        if (automatool.isEmpty())
            return;

        ItemStack automatoolStack = automatool.get().getStack();

        if (!automatoolStack.isOf(AutomataItems.AUTOMATOOL))
            return;

        Optional<BotTeam> deviceTeam = automatool.get().getTeamUuid().map(uuid -> BotTeamPersistentState.getTeam(uuid).orElse(null));
        if (deviceTeam.isPresent() && !deviceTeam.get().hasMemberWith(serverPlayer, BotTeamPermission.CHANGE_TOOL_MODE))
            return;

        AutomatoolMode mode = AutomataRegistries.AUTOMATOOL_MODE.get(message.modeId);

        if (mode == null)
            return;

        AutomatoolMode oldMode = AutomatoolModeComponent.getOrDefault(automatool.get().getStack());

        if (mode == oldMode)
            return;

        AutomatoolModeComponent.set(automatoolStack, mode);
        if (mode.maxSelectionsInt() < oldMode.maxSelectionsInt()) {
            SelectionComponent.set(automatoolStack, Optional.empty());
        }

        AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(serverPlayer).send(message);
    }
}
