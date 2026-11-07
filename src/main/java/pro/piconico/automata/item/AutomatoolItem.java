package pro.piconico.automata.item;

import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
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
        AutomatoolMode mode = AutomatoolModeComponent.getOrDefault(automatoolStack);

        if (!mode.requiresSelection())
            return ActionResult.PASS;

        World world = player.getEntityWorld();

        if (world.isClient())
            return ActionResult.SUCCESS;

        if (!new ItemBotDevice(player, automatoolStack).hasPermission(BotTeamPermission.SELECT, true))
            return ActionResult.FAIL;

        if (selection.toInt() > Selection.TWO.toInt()) {
            selection = Selection.TWO;
        }
        Optional<SelectionComponent> sameWorldSelection = SelectionComponent.get(automatoolStack).filter(s -> s.worldKey().equals(world.getRegistryKey()));
        Optional<BlockPos> pos1 = Optional.ofNullable(selection == Selection.ONE ? pos : sameWorldSelection.map(s -> s.selection1().orElse(null)).orElse(null));
        Optional<BlockPos> pos2 = Optional.ofNullable(selection == Selection.TWO ? pos : sameWorldSelection.map(s -> s.selection2().orElse(null)).orElse(null));
        SelectionComponent.set(automatoolStack, Optional.of(new SelectionComponent(world.getRegistryKey(), pos1, pos2)));

        player.sendMessage(AutomataTexts.getBlockSelected(selection.toInt(), pos), true);

        return ActionResult.SUCCESS;
    }

    private static ActionResult dispatch(PlayerEntity player, ItemStack automatoolStack) {
        AutomatoolMode mode = AutomatoolModeComponent.getOrDefault(automatoolStack);
        Optional<UUID> teamUuid = BotTeamComponent.get(automatoolStack);
        Optional<SelectionComponent> selectionComponent = SelectionComponent.get(automatoolStack);

        Optional<Text> requirementFailure = AutomatoolMode.getRequirementFailure(mode, player, teamUuid, selectionComponent);
        if (requirementFailure.isPresent()) {
            player.sendMessage(requirementFailure.get(), true);
            return ActionResult.FAIL;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return ActionResult.SUCCESS;

        Optional<Text> outcomeText = mode.dispatch().apply(serverPlayer, teamUuid, selectionComponent);
        SelectionComponent.set(automatoolStack, Optional.empty());
        outcomeText.ifPresent(text -> player.sendMessage(text, true));

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
