package pro.piconico.automata.block;

import java.util.Optional;
import java.util.SequencedMap;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pro.piconico.automata.bot.device.BlockBotDevice;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.component.BotTeamComponent;
import pro.piconico.automata.entity.LivingEntityUtils;
import pro.piconico.automata.world.BotTeamPersistentState;

public abstract class BotDeviceBlock<T extends BlockBotDevice> extends BlockWithEntity {
    protected BotDeviceBlock(Settings settings) {
        super(settings.pistonBehavior(PistonBehavior.BLOCK));
    }

    public abstract BlockEntityType<T> getEntityType();

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.isClient() || !(placer instanceof ServerPlayerEntity serverPlayer))
            return;

        SequencedMap<Hand,ItemStack> teamStacks = LivingEntityUtils.getHeldStacks(serverPlayer, BotTeamComponent::exists);

        if (teamStacks.isEmpty())
            return;

        Optional<UUID> teamUuid = BotTeamComponent.get(teamStacks.firstEntry().getValue());

        if (!BotTeamPersistentState.teamExistsAndHasMemberWith(teamUuid.get(), serverPlayer, BotTeamPermission.CHANGE_DEVICE_TEAM))
            return;

        world.getBlockEntity(pos, getEntityType()).get().setTeamUuid(teamUuid);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        T blockEntity = world.getBlockEntity(pos, getEntityType()).get();
        return blockEntity.openScreen(player);
    }
}
