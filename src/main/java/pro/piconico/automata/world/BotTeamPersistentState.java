package pro.piconico.automata.world;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.SortedBotTeamMap;
import pro.piconico.automata.bot.team.BotTeam.BotTeamSettings;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.registry.AutomataPersistentStates;

public class BotTeamPersistentState extends PersistentState {
    public enum Mutation {
        ADD, SETTINGS, MEMBERS, ROLES, REMOVE;

        public static Mutation from(BotTeam.Mutation mutation) {
            return switch (mutation) {
            case BotTeam.Mutation.SETTINGS -> SETTINGS;
            case BotTeam.Mutation.MEMBERS -> MEMBERS;
            case BotTeam.Mutation.ROLES -> ROLES;
            };
        }
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

    public static boolean teamExistsAndHasMemberWith(UUID teamUuid, ServerPlayerEntity serverPlayer, BotTeamPermission permission) {
        return getTeam(teamUuid).filter(team -> team.hasMemberWith(serverPlayer, permission)).isPresent();
    }

    public static boolean teamExistsAndHasMemberWithRead(UUID teamUuid, ServerPlayerEntity serverPlayer) {
        return getTeam(teamUuid).filter(team -> team.hasMemberWithRead(serverPlayer)).isPresent();
    }

    public static Set<UUID> getTeamUuids() {
        return getTeamState().teamMap.keySet();
    }

    public static Collection<BotTeam> getTeams() {
        return Collections.unmodifiableCollection(getTeamState().teamMap.values());
    }

    public static Optional<BotTeam> addTeam(BotTeamSettings settings, Optional<UUID> ownerUuid) {
        if (!BotTeam.isValidName(settings.name()))
            return Optional.empty();

        BotTeamPersistentState teamState = getTeamState();
        BotTeam team = new BotTeam(settings, ownerUuid.map(uuid -> server.getPlayerManager().getPlayer(uuid)));
        teamState.teamMap.put(team.uuid, team);

        teamState.markDirty();
        TEAMS_MUTATED.invoker().onMutate(server, team, Mutation.ADD);

        return Optional.of(team);
    }

    public static Optional<BotTeam> updateTeam(UUID teamUuid, BotTeamSettings settings) {
        Optional<BotTeam> existingTeam = getTeam(teamUuid);

        if (existingTeam.isEmpty())
            return Optional.empty();

        existingTeam.get().setSettings(settings);

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

    private static void onBotTeamMutated(BotTeam team, BotTeam.Mutation mutation) {
        getTeamState().markDirty();
        TEAMS_MUTATED.invoker().onMutate(server, team, Mutation.from(mutation));
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
