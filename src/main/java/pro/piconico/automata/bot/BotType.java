package pro.piconico.automata.bot;

import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.entity.BotEntity;

public record BotType(
    Item item,
    EntityType<? extends BotEntity> entityType,
    Set<BotJobType<?>> supportedJobTypes
) {
}
