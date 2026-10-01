package pro.piconico.automata.bot.job;

import java.util.Set;
import java.util.function.Predicate;
import com.mojang.serialization.Codec;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataRegistries;

public interface BotJob {
    public enum TickResult {
        Failed, Succeeded, Pending;
    }

    public static Codec<BotJob> CODEC = AutomataRegistries.BOT_JOB_TYPE.getCodec().dispatch(BotJob::getType, BotJobType::codec);

    public BlockPos pos();

    public BotJobType<?> getType();

    public Set<Predicate<ItemStack>> getRequiredStackPredicates(World world);

    public Set<Predicate<ItemStack>> getPreferredStackPredicates(World world);

    public boolean canStart(World world);

    public TickResult tick(ServerWorld serverWorld, BotEntity bot, int tick);

    public default void stop(ServerWorld serverWorld, BotEntity bot) {
    }
}
