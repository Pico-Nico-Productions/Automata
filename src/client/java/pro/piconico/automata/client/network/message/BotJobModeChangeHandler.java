package pro.piconico.automata.client.network.message;

import io.wispforest.owo.network.ClientAccess;
import net.minecraft.item.ItemStack;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.client.gui.screen.BotDeviceScreen;
import pro.piconico.automata.component.BotJobModeComponent;
import pro.piconico.automata.network.message.BotJobModeChangeMessage;
import pro.piconico.automata.registry.AutomataRegistries;

public class BotJobModeChangeHandler {
    public static void handle(BotJobModeChangeMessage message, ClientAccess clientAccess) {
        ItemStack stack = clientAccess.player().getInventory().getStack(message.slot());
        BotJobType<?> jobType = AutomataRegistries.BOT_JOB_TYPE.get(message.jobTypeId());
        BotJobModeComponent.set(stack, jobType);

        if (clientAccess.runtime().currentScreen instanceof BotDeviceScreen botDeviceScreen) {
            botDeviceScreen.rebuildTab();
        }
    }
}
