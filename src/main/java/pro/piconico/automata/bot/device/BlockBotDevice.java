package pro.piconico.automata.bot.device;

import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.network.message.BotDeviceTeamChangeMessage;
import pro.piconico.automata.registry.AutomataBotDevices;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.world.BotTeamPersistentState;

public abstract class BlockBotDevice extends BlockEntity implements BotDevice<BlockPos> {
    public record BlockId(BlockPos pos) implements BotDevice.Id {
        public static final MapCodec<BlockId> CODEC = RecordCodecBuilder
                .mapCodec(instance -> instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(BlockId::pos)).apply(instance, BlockId::new));

        @Override
        public BotDeviceType<?, ?> getType() {
            return AutomataBotDevices.BLOCK;
        }
    }

    private static final String TEAM_UUID_KEY = "team_uuid";

    private Optional<UUID> teamUuid = Optional.empty();

    protected BlockBotDevice(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof BlockBotDevice blockDevice))
            return false;

        return world == blockDevice.world && pos.equals(blockDevice.pos);
    }

    //#region BlockEntity
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

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

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    //#endregion

    //#region BotDevice
    @Override
    public Text getDisplayName() {
        return getCachedState().getBlock().getName();
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return pos;
    }

    @Override
    public BlockId getDeviceId() {
        return new BlockId(pos);
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

        BotDevice.invokeTeamChanged(world, this, oldTeamUuid);
        if (world instanceof ServerWorld) {
            AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(this).send(new BotDeviceTeamChangeMessage(getDeviceId(), teamUuid));
        }

        markDirty();

        return true;
    }
    //#endregion

    public static Optional<BlockBotDevice> resolve(PlayerEntity player, BlockId deviceId) {
        return Optional.ofNullable((BlockBotDevice)player.getEntityWorld().getBlockEntity(deviceId.pos));
    }
}
