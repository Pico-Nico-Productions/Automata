package pro.piconico.automata.bot.team;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamRole;

public class BotTeam {
    public static final int MAX_NAME_LENGTH = 16;

    public static boolean isValidName(String name) {
        return !name.strip().isEmpty() && name.length() <= MAX_NAME_LENGTH;
    }

    private static void validateName(String name) {
        if (!isValidName(name))
            throw new IllegalArgumentException("\"" + name + "\" is an invalid " + BotTeam.class.getSimpleName() + " name");
    }

    public static record BotTeamSettings(String name) {
        public static final Endec<BotTeamSettings> ENDEC = CodecUtils.toEndec(RecordCodecBuilder.create(instance -> instance.group( //
                Codec.STRING.fieldOf("name").forGetter(BotTeamSettings::name) //
        ).apply(instance, BotTeamSettings::new)));

        public BotTeamSettings {
            validateName(name);
        }

        @Override
        public final boolean equals(Object object) {
            if (this == object)
                return true;

            if (!(object instanceof BotTeamSettings settings))
                return false;

            return name.equals(settings.name);
        }
    }

    @FunctionalInterface
    public interface Mutate {
        void onMutate(BotTeam team);
    }

    public static final Event<Mutate> MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (team) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(team);
        }
    });

    public static final Codec<BotTeam> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
            Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(team -> team.uuid), //
            Codec.STRING.fieldOf("name").forGetter(BotTeam::getName), //
            BotTeamMember.CODEC.listOf().fieldOf("members").forGetter(team -> List.copyOf(team.members.values())) //
    ).apply(instance, BotTeam::new));
    public static final Endec<BotTeam> ENDEC = CodecUtils.toEndec(CODEC);

    private String name;
    private final Map<UUID, BotTeamMember> members;

    public final UUID uuid;

    private BotTeam(UUID uuid, String name, List<BotTeamMember> members) {
        validateName(name);

        this.uuid = uuid;
        this.name = name;
        this.members = members.stream().collect(Collectors.toMap(member -> member.uuid, member -> member));
    }

    public BotTeam(BotTeamSettings settings, Optional<ServerPlayerEntity> owner) {
        this(UUID.randomUUID(), settings.name, owner.map(own -> List.of(new BotTeamMember(own, BotTeamRole.OWNER))).orElse(List.of()));
    }

    public String getName() {
        return name;
    }

    public BotTeamSettings getSettings() {
        return new BotTeamSettings(name);
    }

    public void setSettings(BotTeamSettings settings) {
        if (getSettings().equals(settings))
            return;

        validateName(settings.name);

        name = settings.name;

        MUTATED.invoker().onMutate(this);
    }

    public void addMember(PlayerEntity player, BotTeamRole role) {
        if (members.containsKey(player.getUuid())) {
            setRole(player.getUuid(), role);
            return;
        }

        members.put(player.getUuid(), new BotTeamMember(player, role));

        MUTATED.invoker().onMutate(this);
    }

    public void addMember(PlayerEntity player) {
        addMember(player, BotTeamRole.USER);
    }

    public void removeMember(UUID playerUuid) {
        if (!members.containsKey(playerUuid))
            return;

        members.remove(playerUuid);

        MUTATED.invoker().onMutate(this);
    }

    public Optional<BotTeamRole> getRole(UUID playerUuid) {
        if (!members.containsKey(playerUuid))
            return Optional.empty();

        return Optional.of(members.get(playerUuid).getRole());
    }

    public void setRole(UUID playerUuid, BotTeamRole role) {
        if (!members.containsKey(playerUuid) || members.get(playerUuid).getRole() == role)
            return;

        members.get(playerUuid).setRole(role);

        MUTATED.invoker().onMutate(this);
    }

    public boolean hasMember(UUID playerUuid) {
        return getRole(playerUuid).isPresent();
    }
}
