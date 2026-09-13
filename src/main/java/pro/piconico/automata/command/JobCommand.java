package pro.piconico.automata.command;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.util.UUIDUtils;
import pro.piconico.automata.world.BotJobPersistentState;
import pro.piconico.automata.world.BotTeamPersistentState;

public class JobCommand {
    private static final String NAME = "job";

    private static final String REMOVE_ARGUMENT = "remove";

    private static final String TEAM_ARGUMENT = "team";
    private static final String WORLD_ARGUMENT = "world";
    private static final String ALL_ARGUMENT = "all";

    private static final String UUID_ARGUMENT = "uuid";

    private static int removeJobs(CommandContext<ServerCommandSource> context, Supplier<Integer> removeJobs) {
        int removeCount = removeJobs.get();

        context.getSource().sendMessage(Text.translatable(AutomataTexts.JOBS_REMOVED, removeCount));

        return removeCount > 0 ? removeCount : 1;
    }

    private static int removeTeamJobs(CommandContext<ServerCommandSource> context) {
        String uuidArgument = StringArgumentType.getString(context, UUID_ARGUMENT);
        Optional<UUID> teamUuid = UUIDUtils.fromString(uuidArgument);

        if (teamUuid.isEmpty() || BotTeamPersistentState.getTeam(teamUuid.get()).isEmpty()) {
            context.getSource().sendMessage(Text.translatable(AutomataTexts.TEAM_MISSING, uuidArgument));
            return 0;
        }

        return removeJobs(context, () -> BotJobPersistentState.removeJobs(context.getSource().getServer(), teamUuid.get()));
    }

    private static int removeWorldJobs(CommandContext<ServerCommandSource> context) {
        return removeJobs(context, () -> BotJobPersistentState.removeJobs(context.getSource().getWorld()));
    }

    private static int removeAllJobs(CommandContext<ServerCommandSource> context) {
        return removeJobs(context, () -> BotJobPersistentState.removeJobs(context.getSource().getServer()));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        LiteralArgumentBuilder<ServerCommandSource> jobCommand = CommandManager.literal(NAME);

        LiteralArgumentBuilder<ServerCommandSource> removeArgument = CommandManager.literal(REMOVE_ARGUMENT);
        removeArgument.then(CommandManager.literal(TEAM_ARGUMENT)
                .then(CommandManager.argument(UUID_ARGUMENT, StringArgumentType.word()).executes(JobCommand::removeTeamJobs)));
        removeArgument.then(CommandManager.literal(WORLD_ARGUMENT).executes(JobCommand::removeWorldJobs));
        removeArgument.then(CommandManager.literal(ALL_ARGUMENT).executes(JobCommand::removeAllJobs));
        jobCommand.then(removeArgument);

        return jobCommand;
    }
}
