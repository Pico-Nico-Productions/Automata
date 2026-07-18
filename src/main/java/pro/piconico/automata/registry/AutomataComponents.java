package pro.piconico.automata.registry;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import pro.piconico.automata.component.CommandToolComponent;

public final class AutomataComponents {
    public static final ComponentType<CommandToolComponent> COMMAND_TOOL = register(AutomataRegistry.COMMAND_TOOL, CommandToolComponent.CODEC, CommandToolComponent.PACKET_CODEC);

    private static <T> ComponentType<T> register(String name, Codec<T> codec, PacketCodec<? super ByteBuf, T> packetCodec) {
        RegistryKey<ComponentType<?>> key = AutomataRegistry.toRegistryKey(RegistryKeys.DATA_COMPONENT_TYPE, name);
        ComponentType<T> componentType = ComponentType.<T>builder().codec(codec).packetCodec(packetCodec).build();
        Registry.register(Registries.DATA_COMPONENT_TYPE, key, componentType);

        return componentType;
    }

    public static void initialize() {
    }
}