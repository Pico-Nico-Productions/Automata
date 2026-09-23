package pro.piconico.automata.registry;

import java.util.Optional;
import java.util.function.BiFunction;
import com.mojang.serialization.MapCodec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.bot.device.BotDeviceType;
import pro.piconico.automata.bot.device.ItemBotDevice;

public class AutomataBotDevices {
    public static final BotDeviceType<BlockBotDevice.Id, BlockBotDevice> BLOCK = register(AutomataRegistry.BLOCK_BOT_DEVICE, BlockBotDevice.Id.CODEC, BlockBotDevice::resolve);
    public static final BotDeviceType<ItemBotDevice.Id, ItemBotDevice> ITEM = register(AutomataRegistry.ITEM_BOT_DEVICE, ItemBotDevice.Id.CODEC, ItemBotDevice::resolve);

    private static <IdT extends BotDevice.Id, BotDeviceT extends BotDevice<?>> BotDeviceType<IdT, BotDeviceT> register(String name, MapCodec<IdT> codec,
            BiFunction<PlayerEntity, IdT, Optional<BotDeviceT>> resolve) {
        Identifier identifier = AutomataRegistry.id(name);
        BotDeviceType<IdT, BotDeviceT> deviceType = Registry.register(AutomataRegistries.BOT_DEVICE_TYPE, identifier, new BotDeviceType<>(codec, resolve));

        return deviceType;
    }

    public static void initialize() {
    }
}
