package pro.piconico.automata.command;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeamMember;
import pro.piconico.automata.bot.team.BotTeam.BotTeamSettings;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamRole;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.util.UUIDUtils;
import pro.piconico.automata.world.BotTeamPersistentState;

public class TeamCommand {
    private static final String NAME = "team";

    private static final String ADD_ARGUMENT = "add";
    private static final String REMOVE_ARGUMENT = "remove";
    private static final String LIST_ARGUMENT = "list";
    private static final String ROLE_ARGUMENT = "role";

    private static final String TEAM_NAME_ARGUMENT = "team_name";
    private static final String TEAM_UUID_ARGUMENT = "team_uuid";
    private static final String TARGETS_ARGUMENT = "targets";
    private static final String ROLE_NAME_ARGUMENT = "role_name";

    public static final SuggestionProvider<ServerCommandSource> ROLE_NAMES = (context, builder) -> {
        for (BotTeamRole role : BotTeamRole.values()) {
            builder.suggest(role.toCamelCase());
        }

        return builder.buildFuture();
    };

    private static Optional<BotTeam> parseTeam(CommandContext<ServerCommandSource> context) {
        String teamUuid = StringArgumentType.getString(context, TEAM_UUID_ARGUMENT);
        Optional<BotTeam> team = UUIDUtils.fromString(teamUuid).map(uuid -> BotTeamPersistentState.getTeam(uuid).orElse(null));

        if (team.isEmpty()) {
            context.getSource().sendMessage(Text.literal(String.format("No such team \"%1$s\"", teamUuid)));
        }

        return team;
    }

    private static MutableText appendTeamText(String string, BotTeam team) {
        return Text.literal(string).append(" \"" + team.getName() + "\" (").append(AutomataTexts.getCopyableText(team.getTruncatedUuid())).append(")");
    }

    private static int addTeam(CommandContext<ServerCommandSource> context) {
        String teamName = StringArgumentType.getString(context, TEAM_NAME_ARGUMENT);

        if (!BotTeam.isValidName(teamName)) {
            context.getSource().sendMessage(Text.literal(String.format("Invalid team name \"%1$s\"", teamName)));
            return 0;
        }

        BotTeamSettings settings = new BotTeamSettings(teamName);
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

    private static MutableText getMemberText(CommandContext<ServerCommandSource> context, BotTeamMember member) {
        return Text.literal(member.getName(context.getSource().getServer()) + " (")
                .append(AutomataTexts.getCopyableText(member.uuid.toString().substring(0, BotTeam.TRUNCATED_UUID_LENGTH)))
                .append(") - " + member.getRole().toPascalCase());
    }

    private static void sendTeamMessage(CommandContext<ServerCommandSource> context, BotTeam team) {
        MutableText teamMessage = appendTeamText("Team", team);

        if (!team.getMembers().isEmpty()) {
            teamMessage.append("\n{");
            for (BotTeamMember member : team.getMembers()) {
                teamMessage.append("\n\t").append(getMemberText(context, member));
            }
            teamMessage.append("\n}");
        }

        context.getSource().sendMessage(teamMessage);
    }

    private static int listTeams(CommandContext<ServerCommandSource> context) {
        Collection<BotTeam> teams = BotTeamPersistentState.getTeams();
        for (BotTeam team : teams) {
            sendTeamMessage(context, team);
        }
        if (teams.isEmpty()) {
            context.getSource().sendMessage(AutomataTexts.getBotTeamsEmpty());
        }

        return teams.size() > 0 ? teams.size() : 1;
    }

    private static int listTeam(CommandContext<ServerCommandSource> context) {
        Optional<BotTeam> team = parseTeam(context);

        if (team.isEmpty())
            return 0;

        sendTeamMessage(context, team.get());

        return 1;
    }

    private static int doActionToTargets(CommandContext<ServerCommandSource> context,
            BiFunction<BotTeam, ServerPlayerEntity, Optional<BotTeamMember>> function) {
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
            Optional<BotTeamMember> member = function.apply(team.get(), serverPlayer);
            if (member.isPresent()) {
                context.getSource().sendMessage(getMemberText(context, member.get()));
            }
            else {
                context.getSource().sendError(Text.literal("Couldn't apply to " + serverPlayer.getStringifiedName()));
            }
        }

        return targets.size();
    }

    private static int addMember(CommandContext<ServerCommandSource> context) {
        return doActionToTargets(context, (team, serverPlayer) -> Optional.of(team.addMember(serverPlayer, BotTeamRole.OWNER)));
    }

    private static int removeMember(CommandContext<ServerCommandSource> context) {
        return doActionToTargets(context, (team, serverPlayer) -> team.removeMember(serverPlayer));
    }

    private static int changeRole(CommandContext<ServerCommandSource> context) {
        String camelCaseRole = StringArgumentType.getString(context, ROLE_NAME_ARGUMENT);
        Optional<BotTeamRole> role = BotTeamRole.parse(camelCaseRole);

        if (role.isEmpty())
            return 0;

        return doActionToTargets(context, (team, serverPlayer) -> team.setRole(serverPlayer, role.get()));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        LiteralArgumentBuilder<ServerCommandSource> teamCommand = CommandManager.literal(NAME);

        LiteralArgumentBuilder<ServerCommandSource> addArgument = CommandManager.literal(ADD_ARGUMENT);
        addArgument.then(CommandManager.argument(TEAM_NAME_ARGUMENT, StringArgumentType.string()).executes(TeamCommand::addTeam));
        teamCommand.then(addArgument);

        LiteralArgumentBuilder<ServerCommandSource> removeArgument = CommandManager.literal(REMOVE_ARGUMENT);
        removeArgument.then(CommandManager.argument(TEAM_UUID_ARGUMENT, StringArgumentType.word()).executes(TeamCommand::removeTeam));
        teamCommand.then(removeArgument);

        LiteralArgumentBuilder<ServerCommandSource> listArgument = CommandManager.literal(LIST_ARGUMENT);
        listArgument.executes(TeamCommand::listTeams);
        teamCommand.then(listArgument);

        RequiredArgumentBuilder<ServerCommandSource, String> teamUuidArgument = CommandManager.argument(TEAM_UUID_ARGUMENT, StringArgumentType.word());
        teamUuidArgument.executes(TeamCommand::listTeam);
        teamUuidArgument.then(CommandManager.literal(ADD_ARGUMENT)
                .then(CommandManager.argument(TARGETS_ARGUMENT, EntityArgumentType.players()).executes(TeamCommand::addMember)));
        teamUuidArgument.then(CommandManager.literal(REMOVE_ARGUMENT)
                .then(CommandManager.argument(TARGETS_ARGUMENT, EntityArgumentType.players()).executes(TeamCommand::removeMember)));
        teamUuidArgument.then(CommandManager.literal(ROLE_ARGUMENT).then(CommandManager.argument(TARGETS_ARGUMENT, EntityArgumentType.players())
                .then(CommandManager.argument(ROLE_NAME_ARGUMENT, StringArgumentType.word()).suggests(ROLE_NAMES).executes(TeamCommand::changeRole))));
        teamCommand.then(teamUuidArgument);

        return teamCommand;
    }
}
