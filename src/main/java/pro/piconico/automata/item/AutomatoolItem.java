package pro.piconico.automata.item;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.component.TeamComponent;
import pro.piconico.automata.entity.LivingEntityUtils;
import pro.piconico.automata.event.HoldItemCallback;
import pro.piconico.automata.network.BotSyncManager;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotTeamPersistentState;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.bot.job.BotJobDispatcher;
import pro.piconico.automata.bot.team.BotTeam;

public class AutomatoolItem extends Item {
    public AutomatoolItem(Settings settings) {
        super(settings.maxCount(1));
    }

    //#region Hold-Based Sync Subscription
    private static void onHoldStarted(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!stack.isOf(AutomataItems.AUTOMATOOL))
            return;

        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return;

        BotSyncManager.updateSubscription(serverPlayer, TeamComponent.get(stack));
    }

    private static void onHoldEnded(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!stack.isOf(AutomataItems.AUTOMATOOL))
            return;

        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return;

        Collection<ItemStack> heldStacks = LivingEntityUtils.getHeldStacks(player, AutomataItems.AUTOMATOOL).values();
        Optional<UUID> teamUuid = heldStacks.stream().map(heldStack -> TeamComponent.get(heldStack).orElse(null)).filter(uuid -> uuid != null)
                .findFirst();
        BotSyncManager.updateSubscription(serverPlayer, teamUuid);
    }
    //#endregion

    //#region Actions
    private static ActionResult select(PlayerEntity player, ItemStack stack, BlockPos selection, boolean isSelection2) {
        if (player.getEntityWorld().isClient())
            return ActionResult.SUCCESS;

        SelectionComponent selectionComponent = SelectionComponent.get(stack);
        stack.set(AutomataComponents.SELECTION, selectionComponent.of(Optional.of(selection), isSelection2));
        player.sendMessage(AutomataTexts.getBlockSelected(isSelection2 ? 2 : 1, selection), true);

        return ActionResult.SUCCESS;
    }

    private static ActionResult openScreen(World world, PlayerEntity player, ItemStack automatoolStack) {
        if (world.isClient())
            return ActionResult.SUCCESS;

        player.openHandledScreen(new ItemBotDevice(automatoolStack));

        return ActionResult.SUCCESS;
    }

    private static ActionResult dispatchCommand(PlayerEntity player, ItemStack automatoolStack) {
        Optional<UUID> teamUuid = TeamComponent.get(automatoolStack);
        SelectionComponent selectionComponent = SelectionComponent.get(automatoolStack);

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return teamUuid.isPresent() && selectionComponent.hasSelection() ? ActionResult.SUCCESS : ActionResult.FAIL;
        }

        if (teamUuid.isEmpty()) {
            player.sendMessage(AutomataTexts.getTeamEmpty(), true);

            return ActionResult.FAIL;
        }

        Optional<BotTeam> team = BotTeamPersistentState.getTeam(teamUuid.get());
        if (team.isEmpty()) {
            if (TeamComponent.set(automatoolStack, Optional.empty())) {
                BotSyncManager.unsubscribe(serverPlayer);
            }
            player.sendMessage(AutomataTexts.getTeamEmpty(), true);

            return ActionResult.FAIL;
        }

        if (!selectionComponent.hasSelection()) {
            player.sendMessage(AutomataTexts.getDeconstructionFailed(), true);

            return ActionResult.FAIL;
        }

        BlockPos selection1 = selectionComponent.selection1().get();
        BlockPos selection2 = selectionComponent.selection2().get();
        Optional<Integer> jobCount = BotJobDispatcher.markForDeconstruction(serverPlayer.getEntityWorld(), teamUuid.get(), selection1, selection2);

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

        return select(player, player.getStackInHand(hand), blockPos, player.isSneaking());
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!player.isSneaking())
            return openScreen(world, player, stack);

        return dispatchCommand(player, stack);
    }
    //#endregion

    public static void initialize() {
        HoldItemCallback.HOLD_STARTED.register(AutomatoolItem::onHoldStarted);
        HoldItemCallback.HOLD_ENDED.register(AutomatoolItem::onHoldEnded);
        AttackBlockCallback.EVENT.register(AutomatoolItem::onAttackBlock);
    }
}
