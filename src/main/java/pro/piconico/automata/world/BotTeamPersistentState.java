package pro.piconico.automata.world;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.SortedBotTeamMap;
import pro.piconico.automata.network.message.BotTeamUpdateC2SMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataPersistentStates;

public class BotTeamPersistentState extends PersistentState {
    public enum Mutation {
        ADD, UPDATE, REMOVE
    }

    @FunctionalInterface
    public interface Mutate {
        void onMutate(MinecraftServer server, BotTeam team, Mutation mutation);
    }

    public static final Event<Mutate> TEAMS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (server, team, mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(server, team, mutation);
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

    public static Set<UUID> getTeamUuids() {
        return getTeamState().teamMap.keySet();
    }

    public static Collection<BotTeam> getTeams() {
        return getTeamState().teamMap.values();
    }

    public static Optional<BotTeam> addTeam(BotTeam team) {
        if (getTeam(team.UUID).isPresent())
            return Optional.empty();

        BotTeamPersistentState teamState = getTeamState();
        teamState.teamMap.put(team.UUID, team);

        teamState.markDirty();
        TEAMS_MUTATED.invoker().onMutate(server, team, Mutation.ADD);

        return Optional.of(team);
    }

    public static Optional<BotTeam> addTeam(String name) {
        if (!BotTeam.isValidName(name))
            return Optional.empty();

        return addTeam(new BotTeam(name));
    }

    public static Optional<BotTeam> updateTeam(BotTeam team) {
        Optional<BotTeam> existingTeam = getTeam(team.UUID);

        if (existingTeam.isEmpty())
            return Optional.empty();

        existingTeam.get().set(team);

        return existingTeam;
    }

    public static Optional<BotTeam> removeTeam(UUID teamUuid) {
        BotTeamPersistentState teamState = getTeamState();

        if (!teamState.teamMap.containsKey(teamUuid))
            return Optional.empty();

        BotTeam team = teamState.teamMap.remove(teamUuid);

        teamState.markDirty();
        TEAMS_MUTATED.invoker().onMutate(server, team, Mutation.REMOVE);

        return Optional.of(team);
    }

    private static void onBotTeamMutated(BotTeam team) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotTeamUpdateC2SMessage(team));
            return;
        }

        getTeamState().markDirty();
        TEAMS_MUTATED.invoker().onMutate(server, team, Mutation.UPDATE);
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
