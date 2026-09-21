package pro.piconico.automata.bot.device;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.network.message.BotTeamSelectMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.world.BotTeamPersistentState;

public abstract class BlockBotDevice extends BlockEntity implements BotDevice<BlockPos> {
    private static final String TEAM_UUID_KEY = "team_uuid";
    private final Set<ChangeBotTeam> listeners = new HashSet<>();

    protected Optional<UUID> teamUuid = Optional.empty();

    public BlockBotDevice(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    //#region BlockEntity
    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        teamUuid = view.read(TEAM_UUID_KEY, Uuids.INT_STREAM_CODEC).filter(uuid -> BotTeamPersistentState.getTeam(uuid).isPresent());
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        teamUuid.ifPresent(uuid -> view.put(TEAM_UUID_KEY, Uuids.INT_STREAM_CODEC, uuid));
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(WrapperLookup registries) {
        return createComponentlessNbt(registries);
    }
    //#endregion

    @Override
    public Text getDisplayName() {
        return getCachedState().getBlock().getName();
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return pos;
    }

    @Override
    public void addTeamChangedListener(ChangeBotTeam listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTeamChangedListener(ChangeBotTeam listener) {
        listeners.remove(listener);
    }

    @Override
    public Optional<UUID> getTeamUuid() {
        return teamUuid;
    }

    @Override
    public boolean setTeamUuid(Optional<UUID> teamUuid) {
        Optional<UUID> oldTeamUuid = this.teamUuid;
        if (oldTeamUuid.equals(teamUuid))
            return false;

        this.teamUuid = teamUuid;

        if (getWorld() instanceof ServerWorld serverWorld) {
            SERVER_TEAM_CHANGED.invoker().onChanged(serverWorld, this, oldTeamUuid);
            AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(this).send(new BotTeamSelectMessage(teamUuid));
        }
        listeners.forEach(listener -> listener.onChanged(oldTeamUuid));

        return true;
    }
}
