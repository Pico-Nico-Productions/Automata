package pro.piconico.automata.registry;

import io.wispforest.owo.network.OwoNetChannel;
import pro.piconico.automata.network.message.BotTeamCreateC2SMessage;
import pro.piconico.automata.network.message.BotTeamDeleteC2SMessage;
import pro.piconico.automata.network.message.BotTeamUpdateC2SMessage;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.network.message.BotDeviceTeamChangeMessage;
import pro.piconico.automata.network.message.BotTeamsSyncC2SMessage;

public class AutomataMessages {
    public static final OwoNetChannel BOT_DEVICE_CHANNEL = OwoNetChannel.create(AutomataRegistry.id(AutomataRegistry.BOT_DEVICE));

    public static void initialize() {
        BOT_DEVICE_CHANNEL.addEndecs(builder -> builder.register(BotDevice.Id.ENDEC, BotDevice.Id.class));

        BOT_DEVICE_CHANNEL.registerServerbound(BotTeamCreateC2SMessage.class, BotTeamCreateC2SMessage::handle);
        BOT_DEVICE_CHANNEL.registerServerbound(BotTeamDeleteC2SMessage.class, BotTeamDeleteC2SMessage::handle);
        BOT_DEVICE_CHANNEL.registerServerbound(BotTeamUpdateC2SMessage.class, BotTeamUpdateC2SMessage::handle);
        BOT_DEVICE_CHANNEL.registerServerbound(BotDeviceTeamChangeMessage.class, BotDeviceTeamChangeMessage::handle);
        BOT_DEVICE_CHANNEL.registerServerbound(BotTeamsSyncC2SMessage.class, BotTeamsSyncC2SMessage::handle);
    }
}
