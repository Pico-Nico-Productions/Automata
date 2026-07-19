package pro.piconico.automata.registry;

import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import pro.piconico.automata.world.BotPersistentState;

public class AutomataPersistentStates {
    public static final PersistentStateType<BotPersistentState> BOT_PERSISTENT_STATE = register(AutomataRegistry.BOT_PERSISTENT_STATE, BotPersistentState::new, BotPersistentState.CODEC); 

    private static <T extends PersistentState> PersistentStateType<T> register(String name, Supplier<T> constructor, Codec<T> codec) {
        return new PersistentStateType<>(name, constructor, codec, null);
    }

    public static void initialize() {
    }

    public static <T extends PersistentState> T get(World world, PersistentStateType<T> stateType) {
        if (world.isClient()) {
            throw new IllegalStateException("Cannot access " + PersistentState.class.getSimpleName() + "s on client.");
        }

        return ((ServerWorld) world).getPersistentStateManager().getOrCreate(stateType);
    }
}
