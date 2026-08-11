package pro.piconico.automata.registry;

import java.util.Set;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import pro.piconico.automata.Automata;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.world.BotPersistentState;

public class AutomataCommands {
    private static final String CLEAR_COMMAND = "clear";

    private static final String NETWORKS_ARGUMENT = "networks";
    private static final String JOBS_ARGUMENT = "jobs";
    private static final String DECONSTRUCTION_ARGUMENT = "deconstruction";

    private static LiteralArgumentBuilder<ServerCommandSource> buildClearCommand() {
        LiteralArgumentBuilder<ServerCommandSource> clearCommand = CommandManager.literal(CLEAR_COMMAND);

        LiteralArgumentBuilder<ServerCommandSource> networksArgument = CommandManager.literal(NETWORKS_ARGUMENT).executes(context -> {
            int clearCount = BotNetworkManager.clearCache(context.getSource().getWorld());

            context.getSource().sendMessage(Text.translatable(AutomataTexts.NETWORKS_REMOVED, clearCount));

            return clearCount > 0 ? clearCount : 1;
        });
        clearCommand.then(networksArgument);

        LiteralArgumentBuilder<ServerCommandSource> jobsArgument = CommandManager.literal(JOBS_ARGUMENT).executes(context -> {
            int removeCount = BotPersistentState.removeJobsOf(AutomataBotJobs.ALL, context.getSource().getWorld());

            context.getSource().sendMessage(Text.translatable(AutomataTexts.JOBS_REMOVED, removeCount));

            return removeCount > 0 ? removeCount : 1;
        });
        LiteralArgumentBuilder<ServerCommandSource> deconstructionArgument = CommandManager.literal(DECONSTRUCTION_ARGUMENT).executes(context -> {
            int removeCount = BotPersistentState.removeJobsOf(Set.of(AutomataBotJobs.DECONSTRUCTION_JOB), context.getSource().getWorld());

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
