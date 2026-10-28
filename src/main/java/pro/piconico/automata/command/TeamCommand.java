package pro.piconico.automata.command;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeam.BotTeamSettings;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamRole;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.util.UUIDUtils;
import pro.piconico.automata.world.BotTeamPersistentState;

public class TeamCommand {
    private static final String NAME = "team";

    private static final String ADD_ARGUMENT = "add";
    private static final String REMOVE_ARGUMENT = "remove";
    private static final String JOIN_ARGUMENT = "join";
    private static final String LEAVE_ARGUMENT = "leave";
    private static final String LIST_ARGUMENT = "list";

    private static final String NAME_ARGUMENT = "name";
    private static final String UUID_ARGUMENT = "uuid";
    private static final String TARGETS_ARGUMENT = "targets";

    private static Optional<BotTeam> parseTeam(CommandContext<ServerCommandSource> context) {
        String uuidArgument = StringArgumentType.getString(context, UUID_ARGUMENT);
        Optional<BotTeam> team = UUIDUtils.fromString(uuidArgument).map(teamUuid -> BotTeamPersistentState.getTeam(teamUuid).orElse(null));

        if (team.isEmpty()) {
            context.getSource().sendMessage(Text.literal(String.format("No such team \"%1$s\"", uuidArgument)));
        }

        return team;
    }

    private static MutableText appendTeamText(String string, BotTeam team) {
        return Text.literal(string).append(" \"" + team.getName() + "\" (").append(AutomataTexts.getCopyableText(team.uuid.toString())).append(")");
    }

    private static int addTeam(CommandContext<ServerCommandSource> context) {
        String name = StringArgumentType.getString(context, NAME_ARGUMENT);

        if (!BotTeam.isValidName(name)) {
            context.getSource().sendMessage(Text.literal(String.format("Invalid team name \"%1$s\"", name)));
            return 0;
        }

        BotTeamSettings settings = new BotTeamSettings(name);
        Optional<BotTeam> team = BotTeamPersistentState.addTeam(settings, Optional.ofNullable(context.getSource().getPlayer()).map(owner -> owner.getUuid()));
        context.getSource().sendMessage(appendTeamText("Added team", team.get()));

        return 1;
    }

    private static int removeTeam(CommandContext<ServerCommandSource> context) {
        Optional<BotTeam> team = parseTeam(context);

        if (team.isEmpty())
            return 0;

        BotTeamPersistentState.removeTeam(team.get().uuid).get();
        context.getSource().sendMessage(appendTeamText("Removed team", team.get()));

        return 1;
    }

    private static int doActionToTargets(CommandContext<ServerCommandSource> context, BiConsumer<BotTeam, ServerPlayerEntity> action, String actionVerb) {
        Optional<BotTeam> team = parseTeam(context);

        if (team.isEmpty())
            return 0;

        Collection<ServerPlayerEntity> targets;
        try {
            targets = EntityArgumentType.getPlayers(context, TARGETS_ARGUMENT);
        }
        catch (IllegalArgumentException noTargetsArgument) {
            ServerPlayerEntity executingPlayer = context.getSource().getPlayer();

            if (executingPlayer == null) {
                context.getSource().sendMessage(Text.literal("No target found"));
                return 0;
            }

            targets = List.of(executingPlayer);
        }
        catch (CommandSyntaxException e) {
            context.getSource().sendError(Text.literal(e.getMessage()));
            return 0;
        }

        for (ServerPlayerEntity serverPlayer : targets) {
            action.accept(team.get(), serverPlayer);
            context.getSource().sendMessage(appendTeamText(serverPlayer.getStringifiedName() + " " + actionVerb + " team", team.get()));
        }

        return targets.size();
    }

    private static int joinTeam(CommandContext<ServerCommandSource> context) {
        return doActionToTargets(context, (team, serverPlayer) -> team.addMember(serverPlayer, BotTeamRole.OWNER), "joined");
    }

    private static int leaveTeam(CommandContext<ServerCommandSource> context) {
        return doActionToTargets(context, (team, serverPlayer) -> team.removeMember(serverPlayer), "left");
    }

    private static int listTeams(CommandContext<ServerCommandSource> context) {
        Collection<BotTeam> teams = BotTeamPersistentState.getTeams();
        for (BotTeam team : teams) {
            context.getSource().sendMessage(appendTeamText("Team", team));
        }
        if (teams.isEmpty()) {
            context.getSource().sendMessage(AutomataTexts.getTeamsEmpty());
        }

        return teams.size() > 0 ? teams.size() : 1;
    }

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        LiteralArgumentBuilder<ServerCommandSource> teamCommand = CommandManager.literal(NAME);

        LiteralArgumentBuilder<ServerCommandSource> addArgument = CommandManager.literal(ADD_ARGUMENT);
        addArgument.then(CommandManager.argument(NAME_ARGUMENT, StringArgumentType.string()).executes(TeamCommand::addTeam));
        teamCommand.then(addArgument);

        LiteralArgumentBuilder<ServerCommandSource> removeArgument = CommandManager.literal(REMOVE_ARGUMENT);
        removeArgument.then(CommandManager.argument(UUID_ARGUMENT, StringArgumentType.word()).executes(TeamCommand::removeTeam));
        teamCommand.then(removeArgument);

        LiteralArgumentBuilder<ServerCommandSource> joinArgument = CommandManager.literal(JOIN_ARGUMENT);
        joinArgument.then(CommandManager.argument(UUID_ARGUMENT, StringArgumentType.word()).executes(TeamCommand::joinTeam) //
                .then(CommandManager.argument(TARGETS_ARGUMENT, EntityArgumentType.players()).executes(TeamCommand::joinTeam)));
        teamCommand.then(joinArgument);

        LiteralArgumentBuilder<ServerCommandSource> leaveArgument = CommandManager.literal(LEAVE_ARGUMENT);
        leaveArgument.then(CommandManager.argument(UUID_ARGUMENT, StringArgumentType.word()).executes(TeamCommand::leaveTeam) //
                .then(CommandManager.argument(TARGETS_ARGUMENT, EntityArgumentType.players()).executes(TeamCommand::leaveTeam)));
        teamCommand.then(leaveArgument);

        LiteralArgumentBuilder<ServerCommandSource> listArgument = CommandManager.literal(LIST_ARGUMENT);
        listArgument.executes(TeamCommand::listTeams);
        teamCommand.then(listArgument);

        return teamCommand;
    }
}
