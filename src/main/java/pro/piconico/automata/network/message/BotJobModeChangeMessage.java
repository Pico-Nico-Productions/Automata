package pro.piconico.automata.network.message;

import io.wispforest.owo.network.ServerAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.component.BotJobModeComponent;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataRegistries;

public record BotJobModeChangeMessage(int slot, Identifier jobTypeId) {
    public BotJobModeChangeMessage(int slot, BotJobType<?> jobType) {
        this(slot, jobType.getRegistryKey().getValue());
    }

    public static void handle(BotJobModeChangeMessage message, ServerAccess serverAccess) {
        if (message.slot < 0 || message.slot >= serverAccess.player().getInventory().size())
            return;

        ItemStack stack = serverAccess.player().getInventory().getStack(message.slot);

        if (stack.isEmpty())
            return;

        BotJobType<?> jobType = AutomataRegistries.BOT_JOB_TYPE.get(message.jobTypeId);

        if (jobType == null)
            return;

        BotJobModeComponent.set(stack, jobType);

        AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(serverAccess.player()).send(message);
    }
}
