package pro.piconico.automata.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.job.BotJobType;

public class AutomataRegistryKeys {
    public static final RegistryKey<Registry<BotType>> BOT = RegistryKey.ofRegistry(AutomataRegistry.id(AutomataRegistry.BOT_JOB));
    public static final RegistryKey<Registry<BotJobType<?>>> BOT_JOB = RegistryKey.ofRegistry(AutomataRegistry.id(AutomataRegistry.BOT_JOB));
}
