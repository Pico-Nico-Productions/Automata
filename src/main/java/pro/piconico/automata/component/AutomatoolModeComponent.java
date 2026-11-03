package pro.piconico.automata.component;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import pro.piconico.automata.bot.mode.AutomatoolMode;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataRegistries;
import pro.piconico.automata.registry.AutomataRegistryKeys;

public record AutomatoolModeComponent(RegistryKey<AutomatoolMode> modeKey) {
    public static final Codec<AutomatoolModeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
            RegistryKey.createCodec(AutomataRegistryKeys.AUTOMATOOL_MODE).fieldOf("mode_key").forGetter(AutomatoolModeComponent::modeKey)) //
            .apply(instance, AutomatoolModeComponent::new));

    public static final PacketCodec<ByteBuf, AutomatoolModeComponent> PACKET_CODEC = PacketCodec.tuple( //
            RegistryKey.createPacketCodec(AutomataRegistryKeys.AUTOMATOOL_MODE), AutomatoolModeComponent::modeKey, //
            AutomatoolModeComponent::new);

    public static AutomatoolMode getDefaultMode() {
        return AutomataRegistries.AUTOMATOOL_MODE.getDefaultEntry().get().value();
    }

    public Optional<AutomatoolMode> getMode() {
        return Optional.ofNullable(AutomataRegistries.AUTOMATOOL_MODE.get(modeKey));
    }

    public static Optional<AutomatoolMode> get(ItemStack stack) {
        AutomatoolModeComponent modeComponent = stack.get(AutomataComponents.AUTOMATOOL_MODE);
        return modeComponent != null ? modeComponent.getMode() : Optional.empty();
    }

    public static AutomatoolMode getOrDefault(ItemStack stack) {
        return get(stack).orElse(getDefaultMode());
    }

    public static void set(ItemStack stack, AutomatoolMode mode) {
        Optional<AutomatoolMode> oldMode = get(stack);

        if (oldMode.filter(j -> j == mode).isPresent())
            return;

        stack.set(AutomataComponents.AUTOMATOOL_MODE, new AutomatoolModeComponent(mode.getRegistryKey()));
    }
}
