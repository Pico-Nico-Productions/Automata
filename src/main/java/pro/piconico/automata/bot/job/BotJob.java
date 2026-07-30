package pro.piconico.automata.bot.job;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.registry.AutomataRegistries;

public interface BotJob {
    BlockPos pos();

    BotJobType<?> getType();

    Codec<BotJob> CODEC = AutomataRegistries.BOT_JOB.getCodec().dispatch(BotJob::getType, BotJobType::codec);
}
