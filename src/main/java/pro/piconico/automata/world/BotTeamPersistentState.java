package pro.piconico.automata.world;

import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.PersistentState;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.SortedBotTeamMap;
import pro.piconico.automata.registry.AutomataPersistentStates;

public class BotTeamPersistentState extends PersistentState {
    public enum Mutation {
        ADD, REMOVE, MODIFY
    }

    @FunctionalInterface
    public interface Mutate {
        void onMutate(Mutation mutation);
    }

    public static final Event<Mutate> TEAMS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(mutation);
        }
    });

    public static final Codec<BotTeamPersistentState> CODEC = SortedBotTeamMap.CODEC.xmap(BotTeamPersistentState::new, state -> state.teamMap).fieldOf("teams")
            .codec();

    private static BotTeamPersistentState instance;

    private final SortedBotTeamMap teamMap;

    public BotTeamPersistentState() {
        teamMap = new SortedBotTeamMap();
    }

    private BotTeamPersistentState(SortedBotTeamMap teamMap) {
        this.teamMap = teamMap;
    }

    public static Optional<BotTeam> getTeam(UUID teamUuid) {
        if (!instance.teamMap.containsKey(teamUuid))
            return Optional.empty();

        return Optional.of(instance.teamMap.get(teamUuid));
    }

    public static SortedBotTeamMap getTeamMap() {
        return instance.teamMap;
    }

    public static Optional<BotTeam> createTeam(String name) {
        if (name.isBlank())
            return Optional.empty();

        BotTeam team = new BotTeam(name);

        instance.teamMap.put(team.UUID, team);
        instance.markDirty();
        TEAMS_MUTATED.invoker().onMutate(Mutation.ADD);

        return Optional.of(team);
    }

    public static Optional<BotTeam> deleteTeam(UUID teamUuid) {
        if (!instance.teamMap.containsKey(teamUuid))
            return Optional.empty();

        BotTeam team = instance.teamMap.remove(teamUuid);

        instance.markDirty();
        TEAMS_MUTATED.invoker().onMutate(Mutation.REMOVE);

        return Optional.of(team);
    }

    private static void onBotTeamMutated(BotTeam team) {
        instance.markDirty();
        TEAMS_MUTATED.invoker().onMutate(Mutation.MODIFY);
    }

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            instance = server.getOverworld().getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_TEAM_PERSISTENT_STATE_TYPE);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            instance = null;
        });

        BotTeam.MUTATED.register(BotTeamPersistentState::onBotTeamMutated);
    }
}
