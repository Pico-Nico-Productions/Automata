package pro.piconico.automata.item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import pro.piconico.automata.component.CommandToolComponent;
import pro.piconico.automata.network.BotSyncManager;
import pro.piconico.automata.registry.AutomataComponents;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotTeamPersistentState;
import pro.piconico.automata.bot.BotDispatcher;
import pro.piconico.automata.bot.team.BotTeam;

public class CommandToolItem extends Item {
    @FunctionalInterface
    public interface ChangeTeam {
        void onChanged(ServerPlayerEntity serverPlayer, Optional<UUID> oldTeamUuid);
    }

    public static final Event<ChangeTeam> TEAM_CHANGED = EventFactory.createArrayBacked(ChangeTeam.class, callbacks -> (serverPlayer, oldTeamUuid) -> {
        for (ChangeTeam callback : callbacks) {
            callback.onChanged(serverPlayer, oldTeamUuid);
        }
    });

    public CommandToolItem(Settings settings) {
        super(settings.maxCount(1));
    }

    private static CommandToolComponent getCommandToolComponent(ItemStack stack) {
        return stack.getOrDefault(AutomataComponents.COMMAND_TOOL, CommandToolComponent.EMPTY);
    }

    public static void onHoldStarted(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!(stack.isOf(AutomataItems.COMMAND_TOOL)))
            return;

        if (!(player instanceof ServerPlayerEntity serverPlayer) || BotSyncManager.isSubscribed(serverPlayer))
            return;

        Optional<UUID> teamUuid = getCommandToolComponent(stack).teamUuid();

        if (teamUuid.isEmpty())
            return;

        BotSyncManager.subscribe(serverPlayer, teamUuid.get());
    }

    public static void onHoldEnded(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!(stack.isOf(AutomataItems.COMMAND_TOOL)))
            return;

        if (player.getMainHandStack().isOf(AutomataItems.COMMAND_TOOL) || player.getOffHandStack().isOf(AutomataItems.COMMAND_TOOL))
            return;

        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return;

        BotSyncManager.unsubscribe(serverPlayer);
    }

    // TODO: Replace with team select screen
    private static ActionResult cycleTeam(PlayerEntity player, ItemStack stack) {
        if (player.getEntityWorld().isClient())
            return ActionResult.SUCCESS;

        CommandToolComponent commandToolComponent = getCommandToolComponent(stack);

        List<BotTeam> sortedTeams = BotTeamPersistentState.getTeamMap().values().stream().toList();
        Optional<Integer> newTeamIndex;
        if (commandToolComponent.teamUuid().isEmpty()) {
            newTeamIndex = Optional.ofNullable(sortedTeams.isEmpty() ? null : 0);
        }
        else {
            UUID currentTeamUuid = commandToolComponent.teamUuid().get();
            Optional<Integer> currentTeamIndex = sortedTeams.stream().filter(team -> team.UUID.equals(currentTeamUuid)).findAny()
                    .map(team -> sortedTeams.indexOf(team));
            newTeamIndex = currentTeamIndex.map(index -> index + 1 < sortedTeams.size() ? index + 1 : null);
        }
        Optional<BotTeam> newTeam = newTeamIndex.map(index -> sortedTeams.get(index));
        Optional<UUID> newTeamUuid = newTeam.map(team -> team.UUID);
        stack.set(AutomataComponents.COMMAND_TOOL, commandToolComponent.of(newTeamUuid));
        player.sendMessage(Text.translatable(AutomataTexts.TEAM_SELECTED, newTeam.map(team -> team.getName()).orElse(BotTeam.EMPTY_BOT_TEAM_STRING)), true);

        if (newTeamUuid.equals(commandToolComponent.teamUuid()))
            return ActionResult.SUCCESS;

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
        if (newTeamUuid.isPresent()) {
            BotSyncManager.subscribe(serverPlayer, newTeamUuid.get());
        }
        else {
            BotSyncManager.unsubscribe(serverPlayer);
        }
        TEAM_CHANGED.invoker().onChanged(serverPlayer, newTeamUuid);

        return ActionResult.SUCCESS;
    }

    private static ActionResult select(PlayerEntity player, ItemStack stack, BlockPos selection, boolean isSelection2) {
        if (player.getEntityWorld().isClient())
            return ActionResult.SUCCESS;

        CommandToolComponent commandToolComponent = getCommandToolComponent(stack);
        stack.set(AutomataComponents.COMMAND_TOOL, commandToolComponent.of(Optional.of(selection), isSelection2));
        player.sendMessage(Text.translatable(AutomataTexts.BLOCK_SELECTED, isSelection2 ? 2 : 1, selection.toShortString()), true);

        return ActionResult.SUCCESS;
    }

    public static ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos blockPos, Direction direction) {
        ItemStack stack = player.getStackInHand(hand);

        if (!(stack.isOf(AutomataItems.COMMAND_TOOL)))
            return ActionResult.PASS;

        return select(player, player.getStackInHand(hand), blockPos, false);
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
        ItemStack stack = player.getStackInHand(hand);

        if (!player.isSneaking())
            return cycleTeam(player, stack);

        return BotDispatcher.markForDeconstruction(player, getCommandToolComponent(stack));
    }
}
