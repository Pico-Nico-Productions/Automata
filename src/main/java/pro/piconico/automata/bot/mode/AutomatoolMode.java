package pro.piconico.automata.bot.mode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.function.TriFunction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.component.SelectionComponent.Selection;
import pro.piconico.automata.registry.AutomataRegistries;
import pro.piconico.automata.registry.AutomataRegistry;

public record AutomatoolMode(Optional<Selection> requiredSelectionCount, TriFunction<ServerWorld, UUID, List<BlockPos>, Text> dispatch) {
    public int getRequiredSelectionCount() {
        return requiredSelectionCount.map(s -> s.toInt()).orElse(0);
    }

    public RegistryKey<AutomatoolMode> getRegistryKey() {
        return AutomataRegistries.AUTOMATOOL_MODE.getKey(this).get();
    }

    public String getTranslationKey() {
        return AutomataRegistry.toAutomatoolModeTranslationKey(getRegistryKey().getValue().getPath());
    }

    public MutableText getName() {
        return Text.translatable(getTranslationKey());
    }
}
