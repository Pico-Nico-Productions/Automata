package pro.piconico.automata.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.job.DeconstructionJob;
import pro.piconico.automata.entity.BotEntity;

public class AutomataBotJobs {
    public static final BotJobType<DeconstructionJob> DECONSTRUCTION_JOB = register(AutomataRegistry.DECONSTRUCTION_JOB, AutomataItems.CONSTRUCTION_BOT, AutomataEntities.CONSTRUCTION_BOT, DeconstructionJob.CODEC);

    private static <T extends BotJob> BotJobType<T> register(String name, Item botItem, EntityType<? extends BotEntity> botEntityType, MapCodec<T> codec) {
        Identifier identifier = AutomataRegistry.id(name);
        return Registry.register(AutomataRegistries.BOT_JOB, identifier, new BotJobType<>(botItem, botEntityType, codec));
    }
}
