package pro.piconico.automata.component;

import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Uuids;
import pro.piconico.automata.registry.AutomataComponents;

public record TeamComponent(UUID uuid) {
    public static final Codec<TeamComponent> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(Uuids.INT_STREAM_CODEC.fieldOf("team_uuid").forGetter(TeamComponent::uuid)).apply(instance, TeamComponent::new));

    public static final PacketCodec<ByteBuf, TeamComponent> PACKET_CODEC = PacketCodec.tuple(Uuids.PACKET_CODEC, component -> component.uuid,
            TeamComponent::new);

    public static Optional<UUID> get(ItemStack stack) {
        TeamComponent teamComponent = stack.get(AutomataComponents.TEAM);
        return Optional.ofNullable(teamComponent != null ? teamComponent.uuid() : null);
    }

    public static boolean set(ItemStack stack, Optional<UUID> teamUuid) {
        Optional<UUID> oldTeamUuid = get(stack);

        if (oldTeamUuid.equals(teamUuid))
            return false;

        if (teamUuid.isEmpty()) {
            stack.remove(AutomataComponents.TEAM);
        }
        else {
            stack.set(AutomataComponents.TEAM, new TeamComponent(teamUuid.get()));
        }

        return true;
    }
}
