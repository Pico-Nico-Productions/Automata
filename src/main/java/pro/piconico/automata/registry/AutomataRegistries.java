package pro.piconico.automata.registry;

import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.job.BotJobType;

public class AutomataRegistries {
    public static final Registry<BotType> BOT_TYPE = new SimpleRegistry<>(AutomataRegistryKeys.BOT_TYPE, Lifecycle.stable());
    public static final Registry<BotJobType<?>> BOT_JOB_TYPE = new SimpleRegistry<>(AutomataRegistryKeys.BOT_JOB_TYPE, Lifecycle.stable());
}
