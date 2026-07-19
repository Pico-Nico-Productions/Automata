package pro.piconico.automata.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public record CommandToolComponent(
    Optional<BlockPos> selection1,
    Optional<BlockPos> selection2
) {
    public static final CommandToolComponent EMPTY = new CommandToolComponent(Optional.empty(), Optional.empty());

    public static final Codec<CommandToolComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.optionalFieldOf("selection1").forGetter(component -> component.selection1),
        BlockPos.CODEC.optionalFieldOf("selection2").forGetter(component -> component.selection2)
        ).apply(instance, (selection1, selection2) -> new CommandToolComponent(selection1, selection2))
    );

    public static final PacketCodec<ByteBuf, CommandToolComponent> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.optional(BlockPos.PACKET_CODEC), component -> component.selection1,
        PacketCodecs.optional(BlockPos.PACKET_CODEC), component -> component.selection2,
        (selection1, selection2) -> new CommandToolComponent(selection1, selection2)
    );

    public Optional<Box> getSelectionBox() {
        if (selection1.isEmpty() || selection2.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(Box.enclosing(selection1.get(), selection2.get()));
    }

    public boolean canDeconstruct() {
        return selection1.isPresent() && selection2.isPresent();
    }
}
