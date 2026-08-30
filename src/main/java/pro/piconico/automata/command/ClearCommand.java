package pro.piconico.automata.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.world.BotJobPersistentState;

public class ClearCommand {
    private static final String NAME = "clear";

    private static final String JOBS_ARGUMENT = "jobs";

    private static int clear(CommandContext<ServerCommandSource> context) {
        int removeCount = BotJobPersistentState.removeJobs(context.getSource().getWorld());

        context.getSource().sendMessage(Text.translatable(AutomataTexts.JOBS_REMOVED, removeCount));

        return removeCount > 0 ? removeCount : 1;
    }

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        LiteralArgumentBuilder<ServerCommandSource> clearCommand = CommandManager.literal(NAME);

        LiteralArgumentBuilder<ServerCommandSource> jobsArgument = CommandManager.literal(JOBS_ARGUMENT).executes(ClearCommand::clear);
        clearCommand.then(jobsArgument);

        return clearCommand;
    }
}
