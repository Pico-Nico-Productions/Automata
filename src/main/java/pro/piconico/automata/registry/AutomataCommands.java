package pro.piconico.automata.registry;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import pro.piconico.automata.Automata;
import pro.piconico.automata.command.JobCommand;
import pro.piconico.automata.command.TeamCommand;

public class AutomataCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> modCommand = CommandManager.literal(Automata.MOD_ID)
                    .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK));

            modCommand.then(TeamCommand.build());
            modCommand.then(JobCommand.build());

            dispatcher.register(modCommand);
        });
    }
}
