package pro.piconico.automata.bot.job;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.function.TriFunction;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.component.SelectionComponent.Selection;
import pro.piconico.automata.registry.AutomataRegistries;
import pro.piconico.automata.registry.AutomataRegistry;

public record BotJobType<T extends BotJob>(MapCodec<T> codec, Optional<Selection> requiredSelectionCount, TriFunction<ServerWorld, UUID, List<BlockPos>, Integer> dispatch) {
    public int getRequiredSelectionCount() {
        return requiredSelectionCount.map(s -> s.toInt()).orElse(0);
    }

    public RegistryKey<BotJobType<?>> getRegistryKey() {
        return AutomataRegistries.BOT_JOB_TYPE.getKey(this).get();
    }

    public String getTranslationKey() {
        return AutomataRegistry.toBotJobTypeTranslationKey(getRegistryKey().getValue().getPath());
    }

    public MutableText getName() {
        return Text.translatable(getTranslationKey());
    }
}
