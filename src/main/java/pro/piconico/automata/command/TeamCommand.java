package pro.piconico.automata.command;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotTeamPersistentState;

public class TeamCommand {
    private static final String NAME = "team";

    private static final String CREATE_ARGUMENT = "create";
    private static final String DELETE_ARGUMENT = "delete";
    private static final String LIST_ARGUMENT = "list";
    private static final String NAME_ARGUMENT = "name";
    private static final String UUID_ARGUMENT = "uuid";

    private static Text getCopyableText(String string) {
        return Text.literal(string).setStyle(AutomataTexts.LINK_STYLE.withClickEvent(new ClickEvent.CopyToClipboard(string)));
    }

    private static int createTeam(CommandContext<ServerCommandSource> context) {
        String name = StringArgumentType.getString(context, NAME_ARGUMENT);
        Optional<BotTeam> team = BotTeamPersistentState.createTeam(name);

        if (team.isEmpty()) {
            context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAM_NAME_INVALID, name));
            return 0;
        }

        context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAM_CREATED, name, getCopyableText(team.get().UUID.toString())));

        return 1;
    }

    private static int deleteTeam(CommandContext<ServerCommandSource> context) {
        UUID teamUuid = UUID.fromString(StringArgumentType.getString(context, UUID_ARGUMENT));
        Optional<BotTeam> team = BotTeamPersistentState.deleteTeam(teamUuid);

        if (team.isEmpty()) {
            context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAM_MISSING, teamUuid.toString()));
            return 0;
        }

        context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAM_DELETED, team.get().getName(), teamUuid.toString()));

        return 1;
    }

    private static int listTeams(CommandContext<ServerCommandSource> context) {
        Collection<BotTeam> teams = BotTeamPersistentState.getTeamMap().values();
        for (BotTeam team : teams) {
            context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAM, team.getName(), getCopyableText(team.UUID.toString())));
        }
        if (teams.isEmpty()) {
            context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAMS_EMPTY));
        }

        return teams.size() > 0 ? teams.size() : 1;
    }

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        LiteralArgumentBuilder<ServerCommandSource> teamCommand = CommandManager.literal(NAME);

        LiteralArgumentBuilder<ServerCommandSource> createArgument = CommandManager.literal(CREATE_ARGUMENT);
        RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = CommandManager.argument(NAME_ARGUMENT, StringArgumentType.string());
        nameArgument.executes(TeamCommand::createTeam);
        createArgument.then(nameArgument);
        teamCommand.then(createArgument);

        LiteralArgumentBuilder<ServerCommandSource> deleteArgument = CommandManager.literal(DELETE_ARGUMENT);
        RequiredArgumentBuilder<ServerCommandSource, String> uuidArgument = CommandManager.argument(UUID_ARGUMENT, StringArgumentType.string());
        uuidArgument.executes(TeamCommand::deleteTeam);
        deleteArgument.then(uuidArgument);
        teamCommand.then(deleteArgument);

        LiteralArgumentBuilder<ServerCommandSource> listArgument = CommandManager.literal(LIST_ARGUMENT);
        listArgument.executes(TeamCommand::listTeams);
        teamCommand.then(listArgument);

        return teamCommand;
    }
}
