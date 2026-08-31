package pro.piconico.automata.world;

import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
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

    private static MinecraftServer server;

    private static BotTeamPersistentState getTeamState() {
        return server.getOverworld().getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_TEAM_PERSISTENT_STATE_TYPE);
    }

    private final SortedBotTeamMap teamMap;

    public BotTeamPersistentState() {
        teamMap = new SortedBotTeamMap();
    }

    private BotTeamPersistentState(SortedBotTeamMap teamMap) {
        this.teamMap = teamMap;
    }

    public static Optional<BotTeam> getTeam(UUID teamUuid) {
        BotTeamPersistentState teamState = getTeamState();

        if (!teamState.teamMap.containsKey(teamUuid))
            return Optional.empty();

        return Optional.of(teamState.teamMap.get(teamUuid));
    }

    public static SortedBotTeamMap getTeamMap() {
        return getTeamState().teamMap;
    }

    public static Optional<BotTeam> createTeam(String name) {
        if (name.isBlank())
            return Optional.empty();

        BotTeamPersistentState teamState = getTeamState();
        BotTeam team = new BotTeam(name);

        teamState.teamMap.put(team.UUID, team);
        teamState.markDirty();
        TEAMS_MUTATED.invoker().onMutate(Mutation.ADD);

        return Optional.of(team);
    }

    public static Optional<BotTeam> deleteTeam(UUID teamUuid) {
        BotTeamPersistentState teamState = getTeamState();

        if (!teamState.teamMap.containsKey(teamUuid))
            return Optional.empty();

        BotTeam team = teamState.teamMap.remove(teamUuid);

        teamState.markDirty();
        TEAMS_MUTATED.invoker().onMutate(Mutation.REMOVE);

        return Optional.of(team);
    }

    private static void onBotTeamMutated(BotTeam team) {
        getTeamState().markDirty();
        TEAMS_MUTATED.invoker().onMutate(Mutation.MODIFY);
    }

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            BotTeamPersistentState.server = server;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            BotTeamPersistentState.server = null;
        });

        BotTeam.MUTATED.register(BotTeamPersistentState::onBotTeamMutated);
    }
}
