package pro.piconico.automata.item;

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

public class CommandToolItem extends Item {
    public CommandToolItem(Settings settings) {
        super(settings.maxCount(1));
    }

    private static CommandToolComponent getCommandToolComponent(ItemStack stack) {
        return stack.getOrDefault(AutomataComponents.COMMAND_TOOL, CommandToolComponent.EMPTY);
    }

    private static void setCommandToolComponent(ItemStack stack, CommandToolComponent data) {
        stack.set(AutomataComponents.COMMAND_TOOL, data);
    }

    public static ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos selection1, Direction direction) {
        ItemStack stack = player.getMainHandStack();
        if (hand != Hand.MAIN_HAND || !(stack.getItem() instanceof CommandToolItem)) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        setCommandToolComponent(stack, new CommandToolComponent(selection1, getCommandToolComponent(stack).selection2()));
        player.sendMessage(Text.translatable(AutomataTexts.SELECTION, 1, selection1.toShortString()), true);

        return ActionResult.SUCCESS;
    }

    private ActionResult sendDeconstructionCommand(PlayerEntity player, CommandToolComponent commandToolComponent) {
        if (!commandToolComponent.canDeconstruct()) {
            player.sendMessage(Text.translatable(AutomataTexts.INVALID_DECONSTRUCTION_SELECTION), true);

            return ActionResult.FAIL;
        }

        // TODO: Send packet/create command here
        setCommandToolComponent(player.getMainHandStack(), CommandToolComponent.EMPTY);
        player.sendMessage(Text.translatable(AutomataTexts.DECONSTRUCTION, commandToolComponent.selection1().toShortString(), commandToolComponent.selection2().toShortString()), true);

        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!player.isSneaking()) {
            return ActionResult.FAIL;
        }

        CommandToolComponent commandToolComponent = getCommandToolComponent(player.getMainHandStack());
        if (world.isClient()) {
            return commandToolComponent.canDeconstruct() ? ActionResult.SUCCESS : ActionResult.FAIL;
        }
        
        return sendDeconstructionCommand(player, commandToolComponent);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient()) {
            return ActionResult.SUCCESS;
        }

        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        if (player.isSneaking()) {
            return sendDeconstructionCommand(player, getCommandToolComponent(stack));
        }

        BlockPos selection2 = context.getBlockPos();
        setCommandToolComponent(stack, new CommandToolComponent(getCommandToolComponent(stack).selection1(), selection2));
        player.sendMessage(Text.translatable(AutomataTexts.SELECTION, 2, selection2.toShortString()), true);

        return ActionResult.SUCCESS;
    }
}
