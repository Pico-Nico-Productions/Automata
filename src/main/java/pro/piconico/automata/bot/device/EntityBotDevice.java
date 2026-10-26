package pro.piconico.automata.bot.device;

import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;
import pro.piconico.automata.network.message.BotDeviceTeamChangeMessage;
import pro.piconico.automata.registry.AutomataBotDevices;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.world.BotTeamPersistentState;

public abstract class EntityBotDevice extends PathAwareEntity implements BotDevice<UUID> {
    public record EntityId(UUID entityUuid) implements BotDevice.Id {
        public static final MapCodec<EntityId> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
                Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(EntityId::entityUuid)) //
                .apply(instance, EntityId::new));

        @Override
        public BotDeviceType<?, ?> getType() {
            return AutomataBotDevices.ENTITY;
        }
    }

    private static final String TEAM_UUID_KEY = "team_uuid";

    protected Optional<UUID> teamUuid = Optional.empty();

    protected EntityBotDevice(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof EntityBotDevice entityDevice))
            return false;

        return uuid.equals(entityDevice.uuid);
    }

    //#region PathAwareEntity
    @Override
    public void readData(ReadView view) {
        super.readData(view);
        teamUuid = view.read(TEAM_UUID_KEY, Uuids.INT_STREAM_CODEC).filter(uuid -> BotTeamPersistentState.getTeam(uuid).isPresent());
    }

    @Override
    public void writeData(WriteView view) {
        super.writeData(view);
        teamUuid.ifPresent(uuid -> view.put(TEAM_UUID_KEY, Uuids.INT_STREAM_CODEC, uuid));
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        return openScreen(player);
    }
    //#endregion

    @Override
    public UUID getScreenOpeningData(ServerPlayerEntity player) {
        return uuid;
    }

    @Override
    public EntityId getDeviceId() {
        return new EntityId(uuid);
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

        World world = getEntityWorld();
        BotDevice.invokeTeamChanged(world, this, oldTeamUuid);
        if (world instanceof ServerWorld) {
            AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(PlayerLookup.tracking(this)).send(new BotDeviceTeamChangeMessage(getDeviceId(), teamUuid));
        }

        return true;
    }

    public static Optional<EntityBotDevice> resolve(PlayerEntity player, EntityId deviceId) {
        return Optional.ofNullable((EntityBotDevice)player.getEntityWorld().getEntity(deviceId.entityUuid));
    }
}
