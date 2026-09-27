package pro.piconico.automata.bot.job;

import com.mojang.serialization.Codec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataRegistries;

public interface BotJob {
    public static Codec<BotJob> CODEC = AutomataRegistries.BOT_JOB_TYPE.getCodec().dispatch(BotJob::getType, BotJobType::codec);

    public BlockPos pos();

    public BotJobType<?> getType();

    public boolean canBeExecuted(ServerWorld serverWorld);

    public default boolean canExecute(ServerWorld serverWorld, BotEntity bot) {
        return canBeExecuted(serverWorld);
    }

    public boolean execute(ServerWorld serverWorld, BotEntity bot);
}
