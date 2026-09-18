package pro.piconico.automata.client.registry;

import pro.piconico.automata.client.network.message.BotTeamSelectHandler;
import pro.piconico.automata.client.network.message.BotTeamsSyncS2CHandler;
import pro.piconico.automata.network.message.BotTeamSelectMessage;
import pro.piconico.automata.network.message.BotTeamsSyncS2CMessage;
import pro.piconico.automata.registry.AutomataMessages;

public class AutomataClientMessageHandlers {
    public static void initialize() {
        AutomataMessages.CHANNEL.registerClientbound(BotTeamSelectMessage.class, BotTeamSelectHandler::handle);
        AutomataMessages.CHANNEL.registerClientbound(BotTeamsSyncS2CMessage.class, BotTeamsSyncS2CHandler::handle);
    }
}
