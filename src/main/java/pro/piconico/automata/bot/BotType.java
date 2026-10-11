package pro.piconico.automata.bot;

import java.util.Set;
import net.minecraft.entity.EntityType;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.item.BotItem;

public record BotType(BotItem item, EntityType<? extends BotEntity> entityType, Set<BotJobType<?>> supportedJobTypes) {
}
