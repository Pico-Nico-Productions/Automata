package pro.piconico.automata.component;

import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Uuids;

public record TeamComponent(UUID uuid) {
    public static final Codec<TeamComponent> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Uuids.INT_STREAM_CODEC.fieldOf("team_uuid").forGetter(TeamComponent::uuid)).apply(instance, TeamComponent::new));

    public static final PacketCodec<ByteBuf, TeamComponent> PACKET_CODEC = PacketCodec.tuple(Uuids.PACKET_CODEC,
            component -> component.uuid, TeamComponent::new);
}
