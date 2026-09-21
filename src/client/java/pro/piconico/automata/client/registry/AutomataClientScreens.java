package pro.piconico.automata.client.registry;

import org.apache.commons.lang3.function.TriFunction;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.client.gui.screen.AutomatoolScreen;
import pro.piconico.automata.client.gui.screen.BotDeviceScreen;
import pro.piconico.automata.client.gui.screen.RoboportScreen;
import pro.piconico.automata.item.AutomatoolItem;
import pro.piconico.automata.registry.AutomataScreenHandlers;
import pro.piconico.automata.screen.ItemBotDeviceScreenHandler;

public class AutomataClientScreens {
    private static BotDeviceScreen<ItemBotDeviceScreenHandler> routeItemBotDeviceScreen(ItemBotDeviceScreenHandler handler, PlayerInventory playerInventory, Text title) {
        ItemBotDevice itemBotDevice = (ItemBotDevice)handler.device;
        TriFunction<ItemBotDeviceScreenHandler, PlayerInventory, Text, BotDeviceScreen<ItemBotDeviceScreenHandler>> factory = switch (itemBotDevice.getItem()) {
        case AutomatoolItem ignored -> AutomatoolScreen::new;
        default -> throw new IllegalArgumentException(ItemBotDeviceScreenHandler.class.getSimpleName() + " was used with an unregistered item");
        };

        return factory.apply(handler, playerInventory, title);
    }

    public static void initialize() {
        HandledScreens.register(AutomataScreenHandlers.ITEM_BOT_DEVICE, AutomataClientScreens::routeItemBotDeviceScreen);
        HandledScreens.register(AutomataScreenHandlers.ROBOPORT, RoboportScreen::new);
    }
}
