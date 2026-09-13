package pro.piconico.automata.command;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.util.UUIDUtils;
import pro.piconico.automata.world.BotTeamPersistentState;

public class TeamCommand {
    private static final String NAME = "team";

    private static final String ADD_ARGUMENT = "add";
    private static final String REMOVE_ARGUMENT = "remove";
    private static final String LIST_ARGUMENT = "list";

    private static final String NAME_ARGUMENT = "name";
    private static final String UUID_ARGUMENT = "uuid";

    private static Text getCopyableText(String string) {
        return Text.literal(string).setStyle(AutomataTexts.LINK_STYLE.withClickEvent(new ClickEvent.CopyToClipboard(string)));
    }

    private static int addTeam(CommandContext<ServerCommandSource> context) {
        String name = StringArgumentType.getString(context, NAME_ARGUMENT);
        Optional<BotTeam> team = BotTeamPersistentState.addTeam(name);

        if (team.isEmpty()) {
            context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAM_NAME_INVALID, name));
            return 0;
        }

        context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAM_ADDED, name, getCopyableText(team.get().UUID.toString())));

        return 1;
    }

    private static int removeTeam(CommandContext<ServerCommandSource> context) {
        String uuidArgument = StringArgumentType.getString(context, UUID_ARGUMENT);
        Optional<UUID> teamUuid = UUIDUtils.fromString(uuidArgument);

        if (teamUuid.isEmpty() || BotTeamPersistentState.getTeam(teamUuid.get()).isEmpty()) {
            context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAM_MISSING, uuidArgument));
            return 0;
        }

        String teamName = BotTeamPersistentState.removeTeam(teamUuid.get()).get().getName();

        context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAM_REMOVED, teamName, uuidArgument));

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

        LiteralArgumentBuilder<ServerCommandSource> addArgument = CommandManager.literal(ADD_ARGUMENT);
        addArgument.then(CommandManager.argument(NAME_ARGUMENT, StringArgumentType.string()).executes(TeamCommand::addTeam));
        teamCommand.then(addArgument);

        LiteralArgumentBuilder<ServerCommandSource> removeArgument = CommandManager.literal(REMOVE_ARGUMENT);
        removeArgument.then(CommandManager.argument(UUID_ARGUMENT, StringArgumentType.word()).executes(TeamCommand::removeTeam));
        teamCommand.then(removeArgument);

        LiteralArgumentBuilder<ServerCommandSource> listArgument = CommandManager.literal(LIST_ARGUMENT);
        listArgument.executes(TeamCommand::listTeams);
        teamCommand.then(listArgument);

        return teamCommand;
    }
}
