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
import pro.piconico.automata.bot.device.EntityBotDevice;
import pro.piconico.automata.bot.device.ItemBotDevice;

public class AutomataBotDevices {
    public static final BotDeviceType<BlockBotDevice.BlockId, BlockBotDevice> BLOCK = register(AutomataRegistry.BLOCK_BOT_DEVICE, BlockBotDevice.BlockId.CODEC, BlockBotDevice::resolve);
    public static final BotDeviceType<EntityBotDevice.EntityId, EntityBotDevice> ENTITY = register(AutomataRegistry.ENTITY_BOT_DEVICE, EntityBotDevice.EntityId.CODEC, EntityBotDevice::resolve);
    public static final BotDeviceType<ItemBotDevice.ItemId, ItemBotDevice> ITEM = register(AutomataRegistry.ITEM_BOT_DEVICE, ItemBotDevice.ItemId.CODEC, ItemBotDevice::resolve);

    private static <IdT extends BotDevice.Id, BotDeviceT extends BotDevice<?>> BotDeviceType<IdT, BotDeviceT> register(String name, MapCodec<IdT> codec,
            BiFunction<PlayerEntity, IdT, Optional<BotDeviceT>> resolve) {
        Identifier identifier = AutomataRegistry.id(name);
        BotDeviceType<IdT, BotDeviceT> deviceType = Registry.register(AutomataRegistries.BOT_DEVICE_TYPE, identifier, new BotDeviceType<>(codec, resolve));

        return deviceType;
    }

    public static void initialize() {
    }
}
