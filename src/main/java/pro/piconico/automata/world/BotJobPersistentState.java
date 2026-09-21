package pro.piconico.automata.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.job.BotJob;
import pro.piconico.automata.bot.job.BotJobAssignment;
import pro.piconico.automata.bot.job.BotJobAssignmentMap;
import pro.piconico.automata.bot.job.BotJobType;
import pro.piconico.automata.bot.network.BotNetworkManager;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.entity.BotEntity;
import pro.piconico.automata.registry.AutomataPersistentStates;
import pro.piconico.automata.util.MapUtils;
import pro.piconico.automata.util.math.ChunkUtils.ChunkBounds;

public class BotJobPersistentState extends PersistentState {
    public enum Mutation {
        ADD, REMOVE, ASSIGN, UNASSIGN
    }

    @FunctionalInterface
    public interface Mutate {
        void onMutate(ServerWorld serverWorld, UUID teamUuid, Mutation mutation);
    }

    public static final Event<Mutate> JOBS_MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (teamUuid, serverWorld, mutation) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(teamUuid, serverWorld, mutation);
        }
    });

    public static final Codec<BotJobPersistentState> CODEC = BotJobAssignment.CODEC.listOf().xmap(BotJobPersistentState::new, BotJobPersistentState::flatten)
            .fieldOf("job_assignment_map").codec();

    private final Map<UUID, BotJobAssignmentMap> teamJobAssignmentMap;
    private final Map<UUID, BotJob> jobLookup;

    public BotJobPersistentState() {
        teamJobAssignmentMap = new HashMap<>();
        jobLookup = new HashMap<>();
    }

    private BotJobPersistentState(List<BotJobAssignment> jobAssignments) {
        this();
        for (BotJobAssignment jobAssignment : jobAssignments) {
            BotJobAssignmentMap jobAssignmentMap = teamJobAssignmentMap.computeIfAbsent(jobAssignment.TEAM_UUID, uuid -> new BotJobAssignmentMap());
            Map<BotJobType<?>, BotJobAssignment> typeMap = jobAssignmentMap.computeIfAbsent(jobAssignment.JOB.pos(), pos -> new HashMap<>());
            typeMap.put(jobAssignment.JOB.getType(), jobAssignment);
            jobAssignment.getAssignedBot().ifPresent(botUuid -> jobLookup.put(botUuid, jobAssignment.JOB));
        }
    }

    private static List<BotJobAssignment> flatten(BotJobPersistentState jobState) {
        return jobState.teamJobAssignmentMap.values().stream().flatMap(jobAssignmentMap -> BotJobAssignmentMap.flatten(jobAssignmentMap).stream()).toList();
    }

    private static BotJobPersistentState getJobState(ServerWorld serverWorld) {
        return serverWorld.getPersistentStateManager().getOrCreate(AutomataPersistentStates.BOT_JOB_PERSISTENT_STATE);
    }

    //#region Job Querying
    public static Optional<BotJob> getJob(ServerWorld serverWorld, UUID botUuid) {
        return MapUtils.getNested(getJobState(serverWorld).jobLookup, botUuid);
    }

    public static Optional<BotJobAssignment> getJobAssignment(ServerWorld serverWorld, UUID teamUuid, BlockPos pos, BotJobType<?> jobType) {
        BotJobPersistentState jobState = getJobState(serverWorld);

        return MapUtils.getNested(jobState.teamJobAssignmentMap, teamUuid, pos, jobType);
    }

    public static Set<BotJobAssignment> getJobAssignments(ServerWorld serverWorld, UUID teamUuid, ChunkBounds chunkBounds) {
        BotJobPersistentState jobState = getJobState(serverWorld);

        if (!jobState.teamJobAssignmentMap.containsKey(teamUuid))
            return Set.of();

        Set<BotJobAssignment> inRangeJobs = new HashSet<>();

        for (Entry<BlockPos, Map<BotJobType<?>, BotJobAssignment>> entry : jobState.teamJobAssignmentMap.get(teamUuid).entrySet()) {
            if (!chunkBounds.containsXZ(entry.getKey()))
                continue;

            inRangeJobs.addAll(entry.getValue().values());
        }

        return inRangeJobs;
    }
    //#endregion

    //#region Job Assignment
    private static boolean assignJob(ServerWorld serverWorld, BotJobAssignment jobAssignment, boolean notify) {
        Optional<BotEntity> botEntity = BotNetworkManager.getOrSpawnBotFor(serverWorld, jobAssignment.TEAM_UUID, jobAssignment.JOB);

        if (botEntity.isEmpty())
            return false;

        UUID botUuid = botEntity.get().getUuid();
        jobAssignment.setAssignedBot(Optional.of(botUuid));
        BotJobPersistentState jobState = getJobState(serverWorld);
        jobState.jobLookup.put(botUuid, jobAssignment.JOB);

        if (notify) {
            jobState.markDirty();
            JOBS_MUTATED.invoker().onMutate(serverWorld, jobAssignment.TEAM_UUID, Mutation.ASSIGN);
        }

        return true;
    }

    private static void assignJobs(ServerWorld serverWorld, UUID teamUuid) {
        BotJobPersistentState jobState = getJobState(serverWorld);
        BotJobAssignmentMap teamMap = jobState.teamJobAssignmentMap.get(teamUuid);

        if (teamMap == null)
            return;

        boolean assigned = false;

        for (Map<BotJobType<?>, BotJobAssignment> typeMap : teamMap.values()) {
            for (BotJobAssignment jobAssignment : typeMap.values()) {
                if (jobAssignment.isAssigned() || !assignJob(serverWorld, jobAssignment, false))
                    continue;

                assigned = true;
            }
        }

        if (!assigned)
            return;

        jobState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.ASSIGN);
    }

    private static void unassignJob(ServerWorld serverWorld, BotJobAssignment jobAssignment, boolean notify) {
        if (!jobAssignment.isAssigned())
            return;

        UUID botUuid = jobAssignment.getAssignedBot().get();
        jobAssignment.setAssignedBot(Optional.empty());
        BotJobPersistentState jobState = getJobState(serverWorld);
        jobState.jobLookup.remove(botUuid);

        if (notify) {
            jobState.markDirty();
            JOBS_MUTATED.invoker().onMutate(serverWorld, jobAssignment.TEAM_UUID, Mutation.UNASSIGN);
        }
    }

    private static void unassignJobs(ServerWorld serverWorld, UUID teamUuid) {
        BotJobPersistentState jobState = getJobState(serverWorld);
        BotJobAssignmentMap teamMap = jobState.teamJobAssignmentMap.get(teamUuid);

        if (teamMap == null)
            return;

        boolean unassigned = false;

        for (Map<BotJobType<?>, BotJobAssignment> typeMap : teamMap.values()) {
            for (BotJobAssignment jobAssignment : typeMap.values()) {
                if (!jobAssignment.isAssigned()
                        || BotNetworkManager.getNetwork(new ChunkPos(jobAssignment.JOB.pos()), jobAssignment.TEAM_UUID, serverWorld).isPresent())
                    continue;

                unassignJob(serverWorld, jobAssignment, false);

                unassigned = true;
            }
        }

        if (!unassigned)
            return;

        jobState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.UNASSIGN);
    }
    //#endregion

    public static Optional<Integer> addJobs(UUID teamUuid, Iterable<BotJob> jobs, ServerWorld serverWorld) {
        if (BotTeamPersistentState.getTeam(teamUuid).isEmpty())
            return Optional.empty();

        BotJobPersistentState jobState = getJobState(serverWorld);
        Set<BlockPos> addedPositions = new HashSet<>();

        Map<BlockPos, Map<BotJobType<?>, BotJobAssignment>> blockMap = jobState.teamJobAssignmentMap.computeIfAbsent(teamUuid,
                uuid -> new BotJobAssignmentMap());
        for (BotJob job : jobs) {
            Map<BotJobType<?>, BotJobAssignment> typeMap = blockMap.computeIfAbsent(job.pos(), pos -> new HashMap<>());
            BotJobType<?> type = job.getType();

            if (typeMap.containsKey(type) && (typeMap.get(type).isAssigned() || typeMap.get(type).JOB.equals(job)))
                continue;

            typeMap.put(type, new BotJobAssignment(job, teamUuid));

            addedPositions.add(job.pos());
        }

        if (addedPositions.isEmpty())
            return Optional.of(0);

        jobState.markDirty();
        JOBS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.ADD);

        assignJobs(serverWorld, teamUuid);

        return Optional.of(addedPositions.size());
    }

    //#region Job Removal
    private static boolean removeJob(ServerWorld serverWorld, UUID teamUuid, BlockPos pos, BotJobType<?> type, boolean notify) {
        BotJobPersistentState jobState = getJobState(serverWorld);
        Optional<BotJobAssignment> jobAssignment = MapUtils.removeNested(jobState.teamJobAssignmentMap, teamUuid, pos, type);

        if (jobAssignment.isEmpty())
            return false;

        unassignJob(serverWorld, jobAssignment.get(), false);

        if (notify) {
            jobState.markDirty();
            JOBS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.REMOVE);
        }

        return true;
    }

    public static int removeJobs(MinecraftServer server, UUID teamUuid) {
        int removeCount = 0;

        for (ServerWorld serverWorld : server.getWorlds()) {
            BotJobPersistentState jobState = getJobState(serverWorld);
            Optional<BotJobAssignmentMap> jobAssigmentMap = MapUtils.getNested(jobState.teamJobAssignmentMap, teamUuid);

            if (jobAssigmentMap.isEmpty())
                continue;

            List<BotJobAssignment> jobAssignments = BotJobAssignmentMap.flatten(jobAssigmentMap.get());
            for (BotJobAssignment jobAssignment : jobAssignments) {
                removeJob(serverWorld, jobAssignment.TEAM_UUID, jobAssignment.JOB.pos(), jobAssignment.JOB.getType(), false);
            }
            removeCount += jobAssignments.size();

            jobState.markDirty();
            JOBS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.REMOVE);
        }

        return removeCount;
    }

    public static int removeJobs(ServerWorld serverWorld) {
        BotJobPersistentState jobState = getJobState(serverWorld);

        if (jobState.teamJobAssignmentMap.isEmpty())
            return 0;

        Set<UUID> removedTeamUuids = Set.copyOf(jobState.teamJobAssignmentMap.keySet());
        List<BotJobAssignment> jobAssignments = flatten(jobState);

        for (BotJobAssignment jobAssignment : jobAssignments) {
            removeJob(serverWorld, jobAssignment.TEAM_UUID, jobAssignment.JOB.pos(), jobAssignment.JOB.getType(), false);
        }

        jobState.markDirty();
        for (UUID teamUuid : removedTeamUuids) {
            JOBS_MUTATED.invoker().onMutate(serverWorld, teamUuid, Mutation.REMOVE);
        }

        return jobAssignments.size();
    }

    public static int removeJobs(MinecraftServer server) {
        int removeCount = 0;

        for (ServerWorld serverWorld : server.getWorlds()) {
            removeCount += removeJobs(serverWorld);
        }

        return removeCount;
    }
    //#endregion

    //#region Event Listeners
    private static void onTeamsMutated(MinecraftServer server, BotTeam team, BotTeamPersistentState.Mutation mutation) {
        if (mutation != BotTeamPersistentState.Mutation.REMOVE)
            return;

        removeJobs(server, team.UUID);
    }

    private static void onNetworksMutated(ServerWorld serverWorld, UUID teamUuid, BotNetworkManager.Mutation mutation) {
        switch (mutation) {
        case BotNetworkManager.Mutation.ADD:
            assignJobs(serverWorld, teamUuid);
            break;
        case BotNetworkManager.Mutation.REMOVE:
            unassignJobs(serverWorld, teamUuid);
            break;
        }
    }

    private static void onBotAdded(RoboportBlockEntity roboport) {
        if (!(roboport.getWorld() instanceof ServerWorld serverWorld) || roboport.getTeamUuid().isEmpty())
            return;

        assignJobs(serverWorld, roboport.getTeamUuid().get());
    }

    private static void onJobEnded(BotEntity botEntity, BotJob job, boolean completed) {
        if (!(botEntity.getEntityWorld() instanceof ServerWorld serverWorld))
            return;

        Optional<BotJobAssignment> jobAssignment = getJobAssignment(serverWorld, botEntity.getTeamUuid().get(), job.pos(), job.getType());
        if (jobAssignment.isEmpty())
            return;

        if (completed) {
            removeJob(serverWorld, botEntity.getTeamUuid().get(), job.pos(), job.getType(), true);
            assignJobs(serverWorld, botEntity.getTeamUuid().get());
        }
        else if (!assignJob(serverWorld, jobAssignment.get(), true)) {
            unassignJob(serverWorld, jobAssignment.get(), true);
        }
    }
    //#endregion

    public static void initialize() {
        BotTeamPersistentState.TEAMS_MUTATED.register(BotJobPersistentState::onTeamsMutated);
        BotNetworkManager.NETWORKS_MUTATED.register(BotJobPersistentState::onNetworksMutated);
        RoboportBlockEntity.BOT_ADDED.register(BotJobPersistentState::onBotAdded);
        BotEntity.JOB_ENDED.register(BotJobPersistentState::onJobEnded);
    }
}
