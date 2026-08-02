package pro.piconico.automata.bot.job;

import com.mojang.serialization.Codec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.registry.AutomataRegistries;

public interface BotJob {
    static Codec<BotJob> CODEC = AutomataRegistries.BOT_JOB.getCodec().dispatch(BotJob::getType, BotJobType::codec);

    BlockPos pos();

    BotJobType<?> getType();

    boolean execute(ServerWorld serverWorld);
}
