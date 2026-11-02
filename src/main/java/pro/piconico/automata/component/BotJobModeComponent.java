package pro.piconico.automata.component;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataRegistries;
import pro.piconico.automata.registry.AutomataRegistryKeys;

public record BotJobModeComponent(RegistryKey<BotJobType<?>> jobTypeKey) {
    public static final Codec<BotJobModeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
            RegistryKey.createCodec(AutomataRegistryKeys.BOT_JOB_TYPE).fieldOf("job_type_key").forGetter(BotJobModeComponent::jobTypeKey)) //
            .apply(instance, BotJobModeComponent::new));

    public static final PacketCodec<ByteBuf, BotJobModeComponent> PACKET_CODEC = PacketCodec.tuple( //
            RegistryKey.createPacketCodec(AutomataRegistryKeys.BOT_JOB_TYPE), BotJobModeComponent::jobTypeKey, //
            BotJobModeComponent::new);

    public static BotJobType<?> getDefaultJobType() {
        return AutomataRegistries.BOT_JOB_TYPE.getDefaultEntry().get().value();
    }

    public Optional<BotJobType<?>> getJobType() {
        return Optional.ofNullable(AutomataRegistries.BOT_JOB_TYPE.get(jobTypeKey));
    }

    public static Optional<BotJobType<?>> get(ItemStack stack) {
        BotJobModeComponent modeComponent = stack.get(AutomataComponents.BOT_JOB_MODE);
        return modeComponent != null ? modeComponent.getJobType() : Optional.empty();
    }

    public static BotJobType<?> getOrDefault(ItemStack stack) {
        return get(stack).orElse(getDefaultJobType());
    }

    public static void set(ItemStack stack, BotJobType<?> jobType) {
        Optional<BotJobType<?>> oldJobType = get(stack);

        if (oldJobType.filter(j -> j == jobType).isPresent())
            return;

        stack.set(AutomataComponents.BOT_JOB_MODE, new BotJobModeComponent(jobType.getRegistryKey()));
    }
}
