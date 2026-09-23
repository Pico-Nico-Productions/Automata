package pro.piconico.automata.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.entity.BotEntity;

public class AutomataBots {
    private static final Map<BotJobType<?>, Set<BotType>> CAPABLE_BOT_TYPES = new HashMap<>();

    public static final BotType CONSTRUCTION_BOT = register(AutomataRegistry.CONSTRUCTION_BOT, AutomataItems.CONSTRUCTION_BOT, AutomataEntities.CONSTRUCTION_BOT, AutomataBotJobs.DECONSTRUCTION);

    private static <T extends BotEntity> BotType register(String name, Item botItem, EntityType<? extends BotEntity> botEntityType,
            BotJobType<?>... supportedJobTypes) {
        Identifier identifier = AutomataRegistry.id(name);
        BotType botType = Registry.register(AutomataRegistries.BOT_TYPE, identifier, new BotType(botItem, botEntityType, Set.of(supportedJobTypes)));

        for (BotJobType<?> botJobType : supportedJobTypes) {
            CAPABLE_BOT_TYPES.computeIfAbsent(botJobType, (bjt) -> new HashSet<>()).add(botType);
        }

        return botType;
    }

    public static Set<BotType> getBotTypesFor(BotJobType<?> botJobType) {
        if (!CAPABLE_BOT_TYPES.containsKey(botJobType))
            return Set.of();

        return CAPABLE_BOT_TYPES.get(botJobType);
    }

    public static Set<BotType> getBotTypesFor(BotJob botJob) {
        return getBotTypesFor(botJob.getType());
    }

    public static void initialize() {
    }
}
