package pro.piconico.automata.bot.job;

import java.util.List;
import com.mojang.serialization.Codec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.item.ItemUtils.PredicateItemStack;
import pro.piconico.automata.registry.AutomataRegistries;

public interface BotJob {
    public enum TickResult {
        FAILED, SUCCEEDED, PENDING;
    }

    public static Codec<BotJob> CODEC = AutomataRegistries.BOT_JOB_TYPE.getCodec().dispatch(BotJob::getType, BotJobType::codec);

    public BlockPos pos();

    public BotJobType<?> getType();

    public boolean canStart(World world);

    public List<PredicateItemStack> getRequiredStacks(World world);

    public default List<PredicateItemStack> getPreferredStacks(World world) {
        return getRequiredStacks(world);
    }

    public TickResult tick(ServerWorld serverWorld, BotEntity bot, int tick);

    public default void stop(ServerWorld serverWorld, BotEntity bot) {
    }
}
