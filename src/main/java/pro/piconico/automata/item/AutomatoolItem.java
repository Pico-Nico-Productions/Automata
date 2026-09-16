package pro.piconico.automata.item;

import java.util.Optional;
import java.util.SequencedMap;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
import pro.piconico.automata.screen.AutomatoolScreenHandler;
import pro.piconico.automata.world.BotTeamPersistentState;
import pro.piconico.automata.bot.job.BotJobDispatcher;
import pro.piconico.automata.bot.team.BotTeam;

public class AutomatoolItem extends Item implements ExtendedScreenHandlerFactory<ItemStack> {
    public AutomatoolItem(Settings settings) {
        super(settings.maxCount(1));
    }

    //#region Team
    private static Optional<UUID> getTeamUuid(ItemStack stack) {
        TeamComponent teamComponent = stack.get(AutomataComponents.TEAM);
        return Optional.ofNullable(teamComponent != null ? teamComponent.uuid() : null);
    }

    //#region Bot Sync Subscription
    private static void updateSubscription(ServerPlayerEntity serverPlayer, Optional<UUID> teamUuid) {
        if (teamUuid.isEmpty()) {
            BotSyncManager.unsubscribe(serverPlayer);
            return;
        }

        BotSyncManager.subscribe(serverPlayer, teamUuid.get());
    }

    private static void onHoldStarted(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!stack.isOf(AutomataItems.AUTOMATOOL))
            return;

        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return;

        updateSubscription(serverPlayer, getTeamUuid(stack));
    }

    private static void onHoldEnded(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!stack.isOf(AutomataItems.AUTOMATOOL))
            return;

        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return;

        SequencedMap<Hand, ItemStack> heldStacks = LivingEntityUtils.getHeldStacks(player, AutomataItems.AUTOMATOOL);
        Optional<UUID> teamUuid = heldStacks.values().stream().map(heldStack -> getTeamUuid(heldStack).orElse(null)).filter(uuid -> uuid != null).findFirst();
        updateSubscription(serverPlayer, teamUuid);
    }
    //#endregion

    private static boolean setTeamUuid(ServerPlayerEntity serverPlayer, ItemStack stack, Optional<UUID> teamUuid) {
        Optional<UUID> oldTeamUuid = getTeamUuid(stack);

        if (oldTeamUuid.equals(teamUuid))
            return false;

        if (teamUuid.isEmpty()) {
            stack.remove(AutomataComponents.TEAM);
        }
        else {
            stack.set(AutomataComponents.TEAM, new TeamComponent(teamUuid.get()));
        }

        updateSubscription(serverPlayer, teamUuid);

        return true;
    }
    //#endregion

    //#region Selection
    private static SelectionComponent getSelectionComponent(ItemStack stack) {
        return stack.getOrDefault(AutomataComponents.SELECTION, SelectionComponent.EMPTY);
    }

    private static ActionResult select(PlayerEntity player, ItemStack stack, BlockPos selection, boolean isSelection2) {
        if (player.getEntityWorld().isClient())
            return ActionResult.SUCCESS;

        SelectionComponent selectionComponent = getSelectionComponent(stack);
        stack.set(AutomataComponents.SELECTION, selectionComponent.of(Optional.of(selection), isSelection2));
        player.sendMessage(Text.translatable(AutomataTexts.BLOCK_SELECTED, isSelection2 ? 2 : 1, selection.toShortString()), true);

        return ActionResult.SUCCESS;
    }
    //#endregion

    private static ActionResult openScreen(World world, PlayerEntity player, ItemStack stack) {
        if (world.isClient())
            return ActionResult.SUCCESS;

        player.openHandledScreen((AutomatoolItem)stack.getItem());

        return ActionResult.SUCCESS;
    }

    private static ActionResult dispatchCommand(PlayerEntity player, ItemStack commandToolStack) {
        Optional<UUID> teamUuid = getTeamUuid(commandToolStack);
        SelectionComponent selectionComponent = getSelectionComponent(commandToolStack);

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return teamUuid.isPresent() && selectionComponent.hasSelection() ? ActionResult.SUCCESS : ActionResult.FAIL;
        }

        if (teamUuid.isEmpty()) {
            player.sendMessage(Text.translatable(AutomataTexts.TEAM_MISSING, BotTeam.EMPTY_UUID), true);

            return ActionResult.FAIL;
        }

        Optional<BotTeam> team = BotTeamPersistentState.getTeam(teamUuid.get());
        if (team.isEmpty()) {
            setTeamUuid(serverPlayer, commandToolStack, Optional.empty());
            player.sendMessage(Text.translatable(AutomataTexts.TEAM_MISSING, BotTeam.EMPTY_UUID), true);

            return ActionResult.FAIL;
        }

        if (!selectionComponent.hasSelection()) {
            player.sendMessage(Text.translatable(AutomataTexts.DECONSTRUCTION_FAILED), true);

            return ActionResult.FAIL;
        }

        BlockPos selection1 = selectionComponent.selection1().get();
        BlockPos selection2 = selectionComponent.selection2().get();
        Optional<Integer> jobCount = BotJobDispatcher.markForDeconstruction(serverPlayer.getEntityWorld(), teamUuid.get(), selection1, selection2);

        commandToolStack.remove(AutomataComponents.SELECTION);
        player.sendMessage(Text.translatable(AutomataTexts.JOBS_ADDED, jobCount.get()), true);

        return ActionResult.SUCCESS_SERVER;
    }

    private static ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos blockPos, Direction direction) {
        ItemStack stack = player.getStackInHand(hand);

        if (!(stack.isOf(AutomataItems.AUTOMATOOL)))
            return ActionResult.PASS;

        return select(player, player.getStackInHand(hand), blockPos, false);
    }

    //#region Item
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (player.isSneaking())
            return dispatchCommand(player, stack);

        return select(player, stack, context.getBlockPos(), true);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!player.isSneaking())
            return openScreen(world, player, stack);

        return dispatchCommand(player, stack);
    }
    //#endregion

    //#region ExtendedScreenHandlerFactory
    @Override
    public Text getDisplayName() {
        return Text.translatable(AutomataItems.AUTOMATOOL.getTranslationKey());
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        ItemStack stack = LivingEntityUtils.getHeldStacks(player, AutomataItems.AUTOMATOOL).firstEntry().getValue();
        return new AutomatoolScreenHandler(syncId, playerInventory, stack);
    }

    @Override
    public ItemStack getScreenOpeningData(ServerPlayerEntity player) {
        return LivingEntityUtils.getHeldStacks(player, AutomataItems.AUTOMATOOL).firstEntry().getValue();
    }
    //#endregion

    public static void initialize() {
        HoldItemCallback.HOLD_STARTED.register(AutomatoolItem::onHoldStarted);
        HoldItemCallback.HOLD_ENDED.register(AutomatoolItem::onHoldEnded);
        AttackBlockCallback.EVENT.register(AutomatoolItem::onAttackBlock);
    }
}
