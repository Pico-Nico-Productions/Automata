package pro.piconico.automata.registry;

import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import pro.piconico.automata.world.BotJobPersistentState;
import pro.piconico.automata.world.BotTeamPersistentState;

public class AutomataPersistentStates {
    public static final PersistentStateType<BotJobPersistentState> BOT_JOB_PERSISTENT_STATE = register(AutomataRegistry.BOT_JOB_PERSISTENT_STATE, BotJobPersistentState::new, BotJobPersistentState.CODEC); 
    public static final PersistentStateType<BotTeamPersistentState> BOT_TEAM_PERSISTENT_STATE_TYPE = register(AutomataRegistry.BOT_TEAM_PERSISTENT_STATE, BotTeamPersistentState::new, BotTeamPersistentState.CODEC);

    private static <T extends PersistentState> PersistentStateType<T> register(String name, Supplier<T> constructor, Codec<T> codec) {
        return new PersistentStateType<>(name, constructor, codec, null);
    }

    public static void initialize() {
        BotJobPersistentState.initialize();
        BotTeamPersistentState.initialize();
    }
}
