package pro.piconico.automata.item;

import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.component.TeamComponent;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.bot.job.BotJobDispatcher;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;

public class AutomatoolItem extends Item {
    public AutomatoolItem(Settings settings) {
        super(settings.maxCount(1));
    }

    //#region Actions
    private static ActionResult select(PlayerEntity player, ItemStack automatoolStack, BlockPos selection, boolean isSelection2) {
        World world = player.getEntityWorld();

        if (world.isClient())
            return ActionResult.SUCCESS;

        if (!new ItemBotDevice(player, automatoolStack).hasPermission(BotTeamPermission.SELECT, true))
            return ActionResult.FAIL;

        automatoolStack.set(AutomataComponents.SELECTION, SelectionComponent.getAndCopyWithOrElse(automatoolStack, world, selection, isSelection2));
        player.sendMessage(AutomataTexts.getBlockSelected(isSelection2 ? 2 : 1, selection), true);

        return ActionResult.SUCCESS;
    }

    private static ActionResult dispatch(PlayerEntity player, ItemStack automatoolStack) {
        Optional<UUID> teamUuid = TeamComponent.get(automatoolStack);
        Optional<SelectionComponent> selectionComponent = SelectionComponent.get(automatoolStack);
        boolean hasSelection = selectionComponent.filter(s -> s.hasSelection()).isPresent();

        if (!(player instanceof ServerPlayerEntity)) {
            return teamUuid.isPresent() && hasSelection ? ActionResult.SUCCESS : ActionResult.FAIL;
        }

        if (!new ItemBotDevice(player, automatoolStack).hasPermission(BotTeamPermission.DISPATCH, true))
            return ActionResult.FAIL;

        if (!hasSelection) {
            player.sendMessage(AutomataTexts.getDeconstructionFailed(), true);

            return ActionResult.FAIL;
        }

        ServerWorld selectionWorld = player.getEntityWorld().getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, selectionComponent.get().worldId()));
        BlockPos selection1 = selectionComponent.get().selection1().get();
        BlockPos selection2 = selectionComponent.get().selection2().get();
        Optional<Integer> jobCount = BotJobDispatcher.markForDeconstruction(selectionWorld, teamUuid.get(), selection1, selection2);

        automatoolStack.remove(AutomataComponents.SELECTION);
        player.sendMessage(AutomataTexts.getJobsAdded(jobCount.get()), true);

        return ActionResult.SUCCESS_SERVER;
    }
    //#endregion

    //#region Inputs
    private static ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos blockPos, Direction direction) {
        ItemStack stack = player.getStackInHand(hand);

        if (!(stack.isOf(AutomataItems.AUTOMATOOL)))
            return ActionResult.PASS;

        return select(player, stack, blockPos, player.isSneaking());
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!player.isSneaking())
            return new ItemBotDevice(player, stack).openScreen(player);

        return dispatch(player, stack);
    }
    //#endregion

    public static void initialize() {
        AttackBlockCallback.EVENT.register(AutomatoolItem::onAttackBlock);
    }
}
