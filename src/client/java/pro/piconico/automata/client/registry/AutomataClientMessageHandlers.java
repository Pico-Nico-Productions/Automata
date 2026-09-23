package pro.piconico.automata.client.registry;

import pro.piconico.automata.client.network.message.BotDeviceTeamChangeHandler;
import pro.piconico.automata.client.network.message.BotTeamsSyncS2CHandler;
import pro.piconico.automata.network.message.BotDeviceTeamChangeMessage;
import pro.piconico.automata.network.message.BotTeamsSyncS2CMessage;
import pro.piconico.automata.registry.AutomataMessages;

public class AutomataClientMessageHandlers {
    public static void initialize() {
        AutomataMessages.BOT_DEVICE_CHANNEL.registerClientbound(BotDeviceTeamChangeMessage.class, BotDeviceTeamChangeHandler::handle);
        AutomataMessages.BOT_DEVICE_CHANNEL.registerClientbound(BotTeamsSyncS2CMessage.class, BotTeamsSyncS2CHandler::handle);
    }
}
