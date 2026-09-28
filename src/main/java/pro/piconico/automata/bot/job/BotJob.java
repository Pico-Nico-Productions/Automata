package pro.piconico.automata.bot.job;

import com.mojang.serialization.Codec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataRegistries;

public interface BotJob {
    public enum TickResult {
        Failed, Succeeded, Pending;
    }

    public static Codec<BotJob> CODEC = AutomataRegistries.BOT_JOB_TYPE.getCodec().dispatch(BotJob::getType, BotJobType::codec);

    public BlockPos pos();

    public BotJobType<?> getType();

    public boolean canStart(ServerWorld serverWorld);

    public TickResult tick(ServerWorld serverWorld, BotEntity bot);
}
