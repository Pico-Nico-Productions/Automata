package pro.piconico.automata.network.message;

import io.wispforest.owo.network.ServerAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import pro.piconico.automata.bot.mode.AutomatoolMode;
import pro.piconico.automata.component.AutomatoolModeComponent;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataRegistries;

public record AutomatoolModeChangeMessage(int slot, Identifier modeId) {
    public AutomatoolModeChangeMessage(int slot, AutomatoolMode mode) {
        this(slot, mode.getRegistryKey().getValue());
    }

    public static void handle(AutomatoolModeChangeMessage message, ServerAccess serverAccess) {
        if (message.slot < 0 || message.slot >= serverAccess.player().getInventory().size())
            return;

        ItemStack stack = serverAccess.player().getInventory().getStack(message.slot);

        if (stack.isEmpty())
            return;

        AutomatoolMode mode = AutomataRegistries.AUTOMATOOL_MODE.get(message.modeId);

        if (mode == null || AutomatoolModeComponent.getOrDefault(stack) == mode)
            return;

        AutomatoolModeComponent.set(stack, mode);

        AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(serverAccess.player()).send(message);
    }
}
