package pro.piconico.automata.registry;

import java.util.HashSet;
import java.util.Set;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.job.DeconstructionJob;

public class AutomataBotJobs {
    public static final Set<BotJobType<?>> ALL = new HashSet<>();
    public static final BotJobType<DeconstructionJob> DECONSTRUCTION_JOB = register(AutomataRegistry.DECONSTRUCTION_JOB, DeconstructionJob.CODEC);

    private static <T extends BotJob> BotJobType<T> register(String name, MapCodec<T> codec) {
        Identifier identifier = AutomataRegistry.id(name);
        BotJobType<T> botJobType = Registry.register(AutomataRegistries.BOT_JOB_TYPE, identifier, new BotJobType<>(codec));
        ALL.add(botJobType);

        return botJobType;
    }
}
