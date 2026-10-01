package pro.piconico.automata.bot.job;

import java.util.Set;
import java.util.function.Predicate;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import pro.piconico.automata.block.BlockUtils;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.inventory.InventoryUtils;
import pro.piconico.automata.registry.AutomataBotJobs;

public record DeconstructionBotJob(BlockPos pos) implements BotJob {
    public static final MapCodec<DeconstructionBotJob> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(job -> job.pos)).apply(instance, DeconstructionBotJob::new));

    @Override
    public BotJobType<?> getType() {
        return AutomataBotJobs.DECONSTRUCTION;
    }

    @Override
    public Set<Predicate<ItemStack>> getRequiredStackPredicates(World world) {
        BlockState state = world.getBlockState(pos);

        if (!state.isToolRequired())
            return Set.of();

        return Set.of(stack -> stack.isSuitableFor(state));
    }

    @Override
    public Set<Predicate<ItemStack>> getPreferredStackPredicates(World world) {
        return Set.of(stack -> stack.isSuitableFor(world.getBlockState(pos)));
    }

    @Override
    public boolean canStart(World world) {
        return BlockUtils.isDeconstructable(world, pos);
    }

    @Override
    public TickResult tick(ServerWorld serverWorld, BotEntity bot) {
        if (!canStart(serverWorld))
            return TickResult.Succeeded;

        // TODO: Simulate breaking like a player
        if (BlockUtils.isBreakableBlock(serverWorld, pos)) {
            if (serverWorld.getBlockEntity(pos) instanceof Inventory inventory && !inventory.isEmpty()) {
                InventoryUtils.add(bot, inventory);
                return TickResult.Pending;
            }

            if (!serverWorld.breakBlock(pos, true, bot))
                return TickResult.Pending;

            bot.addItemsToPickUp(serverWorld.getEntitiesByClass(ItemEntity.class, new Box(pos), entity -> true));
        }
        if (BlockUtils.isFluidSourceBlock(serverWorld, pos)) {
            serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        }

        return TickResult.Succeeded;
    }
}
