package pro.piconico.automata.registry;

import java.util.Set;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import pro.piconico.automata.Automata;
import pro.piconico.automata.world.BotPersistentState;

public class AutomataCommands {
    public static final String CLEAR_COMMAND = "clear";
    public static final String JOBS_ARGUMENT = "jobs";
    public static final String DECONSTRUCTION_ARGUMENT = "deconstruction";

    private static LiteralArgumentBuilder<ServerCommandSource> buildClearCommand() {
        LiteralArgumentBuilder<ServerCommandSource> clearCommand = CommandManager.literal(CLEAR_COMMAND);

        LiteralArgumentBuilder<ServerCommandSource> jobsArgument = CommandManager.literal(JOBS_ARGUMENT);

        jobsArgument.executes(context -> {
            int removeCount = BotPersistentState.clearJobs(context.getSource().getWorld(), AutomataBotJobs.ALL);

            context.getSource().sendMessage(Text.translatable(AutomataTexts.JOBS_REMOVED, removeCount));
            
            return removeCount > 0 ? removeCount : 1;
        });

        LiteralArgumentBuilder<ServerCommandSource> deconstructionArgument = CommandManager.literal(DECONSTRUCTION_ARGUMENT).executes(context -> {
            int removeCount = BotPersistentState.clearJobs(context.getSource().getWorld(), Set.of(AutomataBotJobs.DECONSTRUCTION_JOB));

            context.getSource().sendMessage(Text.translatable(AutomataTexts.JOBS_REMOVED, removeCount));

            return removeCount > 0 ? removeCount : 1;
        });
        jobsArgument.then(deconstructionArgument);

        clearCommand.then(jobsArgument);

        return clearCommand;
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> modCommand = CommandManager.literal(Automata.MOD_ID)
                    .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK));

            modCommand.then(buildClearCommand());

            dispatcher.register(modCommand);
        });
    }
}
