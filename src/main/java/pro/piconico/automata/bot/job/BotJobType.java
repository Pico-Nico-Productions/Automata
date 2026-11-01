package pro.piconico.automata.bot.job;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.function.TriFunction;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.component.SelectionComponent.Selection;

public record BotJobType<T extends BotJob>(MapCodec<T> codec, Optional<Selection> requiredSelectionCount, TriFunction<ServerWorld, UUID, List<BlockPos>, Integer> dispatch) {
}
