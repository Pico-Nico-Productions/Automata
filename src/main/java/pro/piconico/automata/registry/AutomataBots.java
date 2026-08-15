package pro.piconico.automata.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
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
    private static final Map<BotJobType<?>, Set<BotType>> capableBotTypes = new HashMap<>(); 

    public static final BotType CONSTRUCTION_BOT = register(AutomataRegistry.CONSTRUCTION_BOT, AutomataItems.CONSTRUCTION_BOT, AutomataEntities.CONSTRUCTION_BOT, Set.of(AutomataBotJobs.DECONSTRUCTION_JOB));

    private static <T extends BotEntity> BotType register(String name, Item botItem, EntityType<? extends BotEntity> botEntityType, Set<BotJobType<?>> supportedJobTypes) {
        Identifier identifier = AutomataRegistry.id(name);
        BotType botType = Registry.register(AutomataRegistries.BOT_TYPE, identifier, new BotType(botItem, botEntityType, supportedJobTypes));
        
        for (BotJobType<?> botJobType : supportedJobTypes) {
            capableBotTypes.computeIfAbsent(botJobType, (bjt) -> new HashSet<>()).add(botType);
        }
        
        return botType;
    }

    public static Optional<Set<BotType>> getBotTypesFor(BotJobType<?> botJobType) {
        if (!capableBotTypes.containsKey(botJobType))
            return Optional.empty();

        return Optional.of(capableBotTypes.get(botJobType));
    }
    
    public static Optional<Set<BotType>> getBotTypesFor(BotJob botJob) { 
        return getBotTypesFor(botJob.getType());
    }
}
