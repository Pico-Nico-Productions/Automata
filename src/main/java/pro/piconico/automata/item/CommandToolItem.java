package pro.piconico.automata.item;

import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import pro.piconico.automata.component.CommandToolComponent;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.bot.BotDispatcher;

public class CommandToolItem extends Item {
    public CommandToolItem(Settings settings) {
        super(settings.maxCount(1));
    }

    private static CommandToolComponent getCommandToolComponent(ItemStack stack) {
        return stack.getOrDefault(AutomataComponents.COMMAND_TOOL, CommandToolComponent.EMPTY);
    }

    private static ActionResult select(PlayerEntity player, ItemStack stack, BlockPos selection, boolean isSelection2) {
        if (player.getEntityWorld().isClient())
            return ActionResult.SUCCESS;
        
        Optional<BlockPos> selection1 = isSelection2 ? getCommandToolComponent(stack).selection1() : Optional.of(selection);
        Optional<BlockPos> selection2 = isSelection2 ? Optional.of(selection) : getCommandToolComponent(stack).selection2();
        stack.set(AutomataComponents.COMMAND_TOOL, new CommandToolComponent(selection1, selection2));
        player.sendMessage(Text.translatable(AutomataTexts.SELECTED, isSelection2 ? 2 : 1, selection.toShortString()), true);

        return ActionResult.SUCCESS;
    }

    public static ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos blockPos, Direction direction) {
        ItemStack stack = player.getMainHandStack();
        if (hand != Hand.MAIN_HAND || !(stack.getItem() instanceof CommandToolItem))
            return ActionResult.PASS;
        
        return select(player, stack, blockPos, false);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        if (player.isSneaking())
            return BotDispatcher.markForDeconstruction(player, getCommandToolComponent(stack));
        
        return select(player, stack, context.getBlockPos(), true);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!player.isSneaking())
            return ActionResult.FAIL;

        CommandToolComponent commandToolComponent = getCommandToolComponent(player.getMainHandStack());
        return BotDispatcher.markForDeconstruction(player, commandToolComponent);
    }
}
