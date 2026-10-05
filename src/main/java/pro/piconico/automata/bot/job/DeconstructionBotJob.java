package pro.piconico.automata.bot.job;

import java.util.Collections;
import java.util.List;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
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
import pro.piconico.automata.item.ItemUtils.PredicateItemStack;
import pro.piconico.automata.registry.AutomataBotJobs;

// TODO: Add silk touch check to predicate
public record DeconstructionBotJob(BlockPos pos) implements BotJob {
    public static final MapCodec<DeconstructionBotJob> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(job -> job.pos)).apply(instance, DeconstructionBotJob::new));
    public static final float TOOL_SPEED = 1F / 30F, NO_TOOL_SPEED = 1F / 100F;

    @Override
    public BotJobType<?> getType() {
        return AutomataBotJobs.DECONSTRUCTION;
    }

    private static List<PredicateItemStack> getToolPredicateItemStacks(BlockState state) {
        return Collections.unmodifiableList(List.of(new PredicateItemStack(stack -> stack.isSuitableFor(state))));
    }

    @Override
    public List<PredicateItemStack> getRequiredStacks(World world) {
        BlockState state = world.getBlockState(pos);

        if (!state.isToolRequired())
            return List.of();

        return getToolPredicateItemStacks(state);
    }

    @Override
    public List<PredicateItemStack> getPreferredStacks(World world) {
        return getToolPredicateItemStacks(world.getBlockState(pos));
    }

    @Override
    public boolean canStart(World world) {
        return BlockUtils.isDeconstructable(world, pos);
    }

    @Override
    public TickResult tick(ServerWorld serverWorld, BotEntity bot, int tick) {
        if (!canStart(serverWorld))
            return TickResult.SUCCEEDED;

        if (BlockUtils.isBreakableBlock(serverWorld, pos)) {
            if (serverWorld.getBlockEntity(pos) instanceof Inventory inventory && !inventory.isEmpty()) {
                InventoryUtils.add(bot, inventory);
                return TickResult.PENDING;
            }

            BlockState state = serverWorld.getBlockState(pos);
            ItemStack toolStack = bot.getEquippedStack(EquipmentSlot.MAINHAND);
            if (toolStack == ItemStack.EMPTY) {
                toolStack = InventoryUtils.getSlot(bot, stack -> stack.isSuitableFor(state)).map(slot -> bot.getStack(slot)).orElse(ItemStack.EMPTY);
                bot.equipStack(EquipmentSlot.MAINHAND, toolStack);
            }
            float toolMultiplier = (!state.isToolRequired() || !toolStack.isEmpty()) ? TOOL_SPEED : NO_TOOL_SPEED;
            float progress = tick * toolStack.getMiningSpeedMultiplier(state) * toolMultiplier / state.getHardness(serverWorld, pos);
            serverWorld.setBlockBreakingInfo(bot.getId(), pos, BlockUtils.getBreakProgress(progress));

            if (progress < 1F || !serverWorld.breakBlock(pos, true, bot))
                return TickResult.PENDING;

            if (toolStack != ItemStack.EMPTY) {
                toolStack.getItem().postMine(toolStack, serverWorld, state, pos, bot);
                bot.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            bot.addItemsToPickUp(serverWorld.getEntitiesByClass(ItemEntity.class, new Box(pos), entity -> true));
        }
        if (BlockUtils.isFluidSourceBlock(serverWorld, pos)) {
            serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        }

        return TickResult.SUCCEEDED;
    }

    @Override
    public void stop(ServerWorld serverWorld, BotEntity bot) {
        serverWorld.setBlockBreakingInfo(bot.getId(), pos, BlockUtils.BREAK_NOT_IN_PROGRESS);
    }
}
