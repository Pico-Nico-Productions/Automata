package pro.piconico.automata.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.job.DeconstructionBotJob;

public class AutomataBotJobs {
    public static final BotJobType<DeconstructionBotJob> DECONSTRUCTION = register(AutomataRegistry.DECONSTRUCTION_BOT_JOB, DeconstructionBotJob.CODEC);

    private static <T extends BotJob> BotJobType<T> register(String name, MapCodec<T> codec) {
        Identifier identifier = AutomataRegistry.id(name);
        BotJobType<T> botJobType = Registry.register(AutomataRegistries.BOT_JOB_TYPE, identifier, new BotJobType<>(codec));

        return botJobType;
    }

    public static void initialize() {
    }
}
