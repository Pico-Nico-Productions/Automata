package pro.piconico.automata.client.ui.tab;

import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.client.ui.render.ButtonComponentRenderers;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public interface BotDeviceHomeTab extends Tab<BotDeviceScreenHandler> {
    @Override
    default Text getName(BotDeviceScreenHandler handler) {
        return switch (handler.device) {
            case ItemBotDevice itemBotDevice -> itemBotDevice.getDisplayName();
            case BlockBotDevice blockBotDevice -> blockBotDevice.getCachedState().getBlock().getName();
            default -> AutomataTexts.getBotDeviceHome();
        };
    }

    @Override
    default ButtonComponent.Renderer getButtonRenderer(BotDeviceScreenHandler handler) {
        return switch (handler.device) {
            case ItemBotDevice itemBotDevice -> ButtonComponentRenderers.item(new ItemStack(itemBotDevice.getItem()));
            case BlockBotDevice blockBotDevice -> ButtonComponentRenderers.block(blockBotDevice.getCachedState());
            default -> AutomataClientTextures.BOT_DEVICE_HOME_ICON.getButtonRenderer();
        };
    }
}
