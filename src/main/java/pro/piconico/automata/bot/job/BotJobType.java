package pro.piconico.automata.bot.job;

import com.mojang.serialization.MapCodec;

public record BotJobType<T extends BotJob>(MapCodec<T> codec) {
}
