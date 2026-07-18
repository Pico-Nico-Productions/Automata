package pro.piconico.automata.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;

public record CommandToolComponent(
    BlockPos selection1,
    BlockPos selection2
) {
    public static final CommandToolComponent EMPTY = new CommandToolComponent(null, null);

    public static final Codec<CommandToolComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.optionalFieldOf("selection1").forGetter(component -> Optional.ofNullable(component.selection1)),
        BlockPos.CODEC.optionalFieldOf("selection2").forGetter(component -> Optional.ofNullable(component.selection2))
        ).apply(instance, (selection1, selection2) -> new CommandToolComponent(selection1.orElse(null), selection2.orElse(null)))
    );

    public static final PacketCodec<ByteBuf, CommandToolComponent> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.optional(BlockPos.PACKET_CODEC), component -> Optional.ofNullable(component.selection1),
        PacketCodecs.optional(BlockPos.PACKET_CODEC), component -> Optional.ofNullable(component.selection2),
        (selection1, selection2) -> new CommandToolComponent(selection1.orElse(null), selection2.orElse(null))
    );

    public boolean canDeconstruct() {
        return selection1 != null && selection2 != null;
    }
}
