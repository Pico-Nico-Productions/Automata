package pro.piconico.automata.bot.device;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import pro.piconico.automata.registry.AutomataRegistries;

public interface BotDevice<T> extends ExtendedScreenHandlerFactory<T> {
    public static interface Id {
        public static final Endec<Id> ENDEC = CodecUtils.toEndec(AutomataRegistries.BOT_DEVICE_TYPE.getCodec().dispatch(Id::getType, BotDeviceType::codec));

        public BotDeviceType<?, ?> getType();
    }

    @FunctionalInterface
    public interface ChangeBotTeam {
        void onChanged(World world, BotDevice<?> device, Optional<UUID> oldTeamUuid);
    }

    public static final Set<ChangeBotTeam> TEAM_CHANGED_CALLBACKS = new HashSet<>();

    public static void invokeTeamChanged(World world, BotDevice<?> device, Optional<UUID> oldTeamUuid) {
        for (ChangeBotTeam changeBotTeam : TEAM_CHANGED_CALLBACKS) {
            changeBotTeam.onChanged(world, device, oldTeamUuid);
        }
    }

    public boolean equals(Object obj);

    public Id getId();

    public Optional<UUID> getTeamUuid();

    public boolean setTeamUuid(Optional<UUID> teamUuid);

    @SuppressWarnings("unchecked")
    public static <IdT extends Id, BotDeviceT extends BotDevice<?>> Optional<BotDeviceT> resolve(PlayerEntity player, IdT deviceId) {
        return ((BotDeviceType<IdT, BotDeviceT>)deviceId.getType()).resolve().apply(player, deviceId);
    }
}
