package pro.piconico.automata.bot.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.bot.device.LogisticStorage;

class NetworkLoader {
    private final Set<ChunkPos> requested = new HashSet<>();
    private final Set<ChunkPos> loaded = new HashSet<>();
    private final Set<BlockPos> roboports = new HashSet<>();
    private final Set<BlockPos> logisticStorages = new HashSet<>();

    public final ServerWorld serverWorld;
    public final UUID teamUuid;

    public NetworkLoader(ServerWorld serverWorld, UUID teamUuid, WorldChunk chunk) {
        this.serverWorld = serverWorld;
        this.teamUuid = teamUuid;
        load(chunk);
    }

    public boolean isComplete() {
        return requested.size() == loaded.size();
    }

    public Set<BlockPos> getRoboports() {
        return Collections.unmodifiableSet(roboports);
    }

    public Set<BlockPos> getLogisticStorages() {
        return Collections.unmodifiableSet(logisticStorages);
    }

    public void load(WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        boolean added = false;
        List<BlockEntity> teamBotDevices = new ArrayList<>();

        requested.add(chunkPos);
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (!(blockEntity instanceof BotDevice<?> device))
                continue;

            boolean wrongTeam = device.getTeamUuid().filter(teamUuid::equals).isEmpty();
            if (!(device instanceof RoboportBlockEntity roboport) || wrongTeam) {
                if (!wrongTeam) {
                    teamBotDevices.add(blockEntity);
                }
                continue;
            }

            added |= roboports.add(roboport.getPos());
        }
        loaded.add(chunkPos);

        if (!added)
            return;

        for (BlockEntity botDevice : teamBotDevices) {
            if (!(botDevice instanceof LogisticStorage<?>))
                continue;

            logisticStorages.add(botDevice.getPos());
        }

        for (ChunkPos pos : ChunkPos.stream(chunkPos, 1).collect(Collectors.toSet())) {
            if (!requested.add(pos))
                continue;

            serverWorld.getChunkManager().addTicket(ChunkTicketType.PLAYER_SPAWN, pos, 0);
        }
    }

    public void process() {
        Set<ChunkPos> toLoad = new HashSet<>(requested);
        toLoad.removeAll(loaded);

        for (ChunkPos chunkPos : toLoad) {
            WorldChunk worldChunk = serverWorld.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z);

            if (worldChunk == null)
                continue;

            load(worldChunk);
        }
    }
}
