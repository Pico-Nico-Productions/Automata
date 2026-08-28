package pro.piconico.automata.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public record CommandToolComponent(Optional<BlockPos> selection1, Optional<BlockPos> selection2, Optional<UUID> teamUuid) {
    public static final Codec<CommandToolComponent> CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(BlockPos.CODEC.optionalFieldOf("selection1").forGetter(CommandToolComponent::selection1),
                            BlockPos.CODEC.optionalFieldOf("selection2").forGetter(CommandToolComponent::selection2),
                            Uuids.INT_STREAM_CODEC.optionalFieldOf("team_uuid").forGetter(CommandToolComponent::teamUuid))
                    .apply(instance, CommandToolComponent::new));

    public static final PacketCodec<ByteBuf, CommandToolComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.optional(BlockPos.PACKET_CODEC),
            component -> component.selection1, PacketCodecs.optional(BlockPos.PACKET_CODEC), component -> component.selection2,
            PacketCodecs.optional(Uuids.PACKET_CODEC), component -> component.teamUuid, CommandToolComponent::new);

    public static final CommandToolComponent EMPTY = new CommandToolComponent(Optional.empty(), Optional.empty(), Optional.empty());

    public CommandToolComponent of(Optional<BlockPos> selection, boolean isSelection2) {
        return new CommandToolComponent(isSelection2 ? selection1 : selection, isSelection2 ? selection : selection2, teamUuid);
    }

    public CommandToolComponent of(Optional<UUID> teamUuid) {
        return new CommandToolComponent(selection1, selection2, teamUuid);
    }

    public boolean hasSelection() {
        return selection1.isPresent() && selection2.isPresent();
    }

    public Optional<Box> getSelectionBox() {
        if (!hasSelection()) {
            return Optional.empty();
        }

        return Optional.of(Box.enclosing(selection1.get(), selection2.get()));
    }
}
