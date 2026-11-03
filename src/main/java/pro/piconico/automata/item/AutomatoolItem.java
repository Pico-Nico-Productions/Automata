package pro.piconico.automata.item;

import java.util.Optional;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.component.SelectionComponent.Selection;
import pro.piconico.automata.component.AutomatoolModeComponent;
import pro.piconico.automata.component.BotTeamComponent;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.bot.device.ItemBotDevice;
import pro.piconico.automata.bot.mode.AutomatoolMode;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;

public class AutomatoolItem extends Item {
    public AutomatoolItem(Settings settings) {
        super(settings.maxCount(1));
    }

    //#region Actions
    private static ActionResult select(PlayerEntity player, ItemStack automatoolStack, BlockPos pos, Selection selection) {
        World world = player.getEntityWorld();

        if (world.isClient())
            return ActionResult.SUCCESS;

        if (!new ItemBotDevice(player, automatoolStack).hasPermission(BotTeamPermission.SELECT, true))
            return ActionResult.FAIL;

        AutomatoolMode mode = AutomatoolModeComponent.getOrDefault(automatoolStack);

        if (mode.requiredSelectionCount().isEmpty())
            return ActionResult.PASS;

        if (mode.getRequiredSelectionCount() < selection.toInt()) {
            selection = mode.requiredSelectionCount().get();
        }
        Optional<SelectionComponent> sameWorldSelection = SelectionComponent.get(automatoolStack).filter(s -> s.worldKey().equals(world.getRegistryKey()));
        Optional<BlockPos> pos1 = Optional.ofNullable(selection == Selection.ONE ? pos : sameWorldSelection.map(s -> s.selection1().orElse(null)).orElse(null));
        Optional<BlockPos> pos2 = Optional.ofNullable(selection != Selection.ONE ? pos : sameWorldSelection.map(s -> s.selection2().orElse(null)).orElse(null));
        SelectionComponent.set(automatoolStack, Optional.of(new SelectionComponent(world.getRegistryKey(), pos1, pos2)));

        player.sendMessage(AutomataTexts.getBlockSelected(selection.toInt(), pos), true);

        return ActionResult.SUCCESS;
    }

    private static ActionResult dispatch(PlayerEntity player, ItemStack automatoolStack) {
        Optional<SelectionComponent> selectionComponent = SelectionComponent.get(automatoolStack);
        AutomatoolMode mode = AutomatoolModeComponent.getOrDefault(automatoolStack);
        int requiredSelectionCount = mode.getRequiredSelectionCount();

        if (selectionComponent.map(s -> s.getSelectionCount()).orElse(0) < requiredSelectionCount) {
            player.sendMessage(AutomataTexts.getSelectionInvalid(requiredSelectionCount), true);

            return ActionResult.FAIL;
        }

        if (!new ItemBotDevice(player, automatoolStack).hasPermission(BotTeamPermission.DISPATCH, true))
            return ActionResult.FAIL;

        if (player.getEntityWorld().isClient())
            return ActionResult.SUCCESS;

        Optional<ServerWorld> selectionWorld = selectionComponent.get().getWorld(player.getEntityWorld().getServer());

        if (selectionWorld.isEmpty()) {
            player.sendMessage(AutomataTexts.getSelectionInvalid(requiredSelectionCount), true);

            return ActionResult.FAIL;
        }

        Text outcomeText = mode.dispatch().apply(selectionWorld.get(), BotTeamComponent.get(automatoolStack).get(), selectionComponent.get().toList());

        SelectionComponent.set(automatoolStack, Optional.empty());
        player.sendMessage(outcomeText, true);

        return ActionResult.SUCCESS_SERVER;
    }
    //#endregion

    //#region Inputs
    private static ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos blockPos, Direction direction) {
        ItemStack stack = player.getStackInHand(hand);

        if (!(stack.isOf(AutomataItems.AUTOMATOOL)))
            return ActionResult.PASS;

        return select(player, stack, blockPos, player.isSneaking() ? Selection.TWO : Selection.ONE);
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
