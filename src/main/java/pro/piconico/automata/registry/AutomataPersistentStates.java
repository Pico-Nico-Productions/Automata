package pro.piconico.automata.registry;

import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import pro.piconico.automata.world.BotPersistentState;

public class AutomataPersistentStates {
    public static final PersistentStateType<BotPersistentState> BOT_PERSISTENT_STATE = register(AutomataRegistry.BOT_PERSISTENT_STATE, BotPersistentState::new, BotPersistentState.CODEC); 

    private static <T extends PersistentState> PersistentStateType<T> register(String name, Supplier<T> constructor, Codec<T> codec) {
        return new PersistentStateType<>(name, constructor, codec, null);
    }

    public static void initialize() {
        BotPersistentState.initialize();
    }
}
