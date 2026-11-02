package pro.piconico.automata.registry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.function.TriFunction;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.job.DeconstructionBotJob;
import pro.piconico.automata.component.SelectionComponent.Selection;

public class AutomataBotJobs {
    public static final BotJobType<DeconstructionBotJob> DECONSTRUCTION = register(AutomataRegistry.DECONSTRUCTION, DeconstructionBotJob.CODEC, Selection.TWO,
            DeconstructionBotJob::dispatch);

    private static <T extends BotJob> BotJobType<T> register(String name, MapCodec<T> codec, Optional<Selection> selectionRequirement,
            TriFunction<ServerWorld, UUID, List<BlockPos>, Integer> dispatch) {
        Identifier identifier = AutomataRegistry.id(name);
        BotJobType<T> botJobType = Registry.register(AutomataRegistries.BOT_JOB_TYPE, identifier, new BotJobType<T>(codec, selectionRequirement, dispatch));

        return botJobType;
    }

    private static <T extends BotJob> BotJobType<T> register(String name, MapCodec<T> codec, Selection selectionRequirement,
            TriFunction<ServerWorld, UUID, List<BlockPos>, Integer> dispatch) {
        return register(name, codec, Optional.of(selectionRequirement), dispatch);
    }

    public static void initialize() {
    }
}
