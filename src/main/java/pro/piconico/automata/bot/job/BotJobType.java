package pro.piconico.automata.bot.job;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import pro.piconico.automata.entity.BotEntity;

public record BotJobType<T extends BotJob>(Item botItem, EntityType<? extends BotEntity> botEntityType, MapCodec<T> codec) {
}
