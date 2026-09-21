package pro.piconico.automata.registry;

import io.wispforest.owo.network.OwoNetChannel;
import pro.piconico.automata.Automata;
import pro.piconico.automata.network.message.BotTeamCreateC2SMessage;
import pro.piconico.automata.network.message.BotTeamDeleteC2SMessage;
import pro.piconico.automata.network.message.BotTeamUpdateC2SMessage;
import pro.piconico.automata.network.message.BotTeamSelectMessage;
import pro.piconico.automata.network.message.BotTeamsSyncC2SMessage;

public class AutomataMessages {
    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(AutomataRegistry.id(Automata.MOD_ID));

    public static void initialize() {
        CHANNEL.registerServerbound(BotTeamCreateC2SMessage.class, BotTeamCreateC2SMessage::handle);
        CHANNEL.registerServerbound(BotTeamDeleteC2SMessage.class, BotTeamDeleteC2SMessage::handle);
        CHANNEL.registerServerbound(BotTeamUpdateC2SMessage.class, BotTeamUpdateC2SMessage::handle);
        CHANNEL.registerServerbound(BotTeamSelectMessage.class, BotTeamSelectMessage::handle);
        CHANNEL.registerServerbound(BotTeamsSyncC2SMessage.class, BotTeamsSyncC2SMessage::handle);
    }
}
