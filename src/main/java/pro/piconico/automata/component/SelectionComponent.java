package pro.piconico.automata.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import pro.piconico.automata.registry.AutomataComponents;

public record SelectionComponent(Optional<BlockPos> selection1, Optional<BlockPos> selection2) {
    public static final Codec<SelectionComponent> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(BlockPos.CODEC.optionalFieldOf("selection1").forGetter(SelectionComponent::selection1),
                    BlockPos.CODEC.optionalFieldOf("selection2").forGetter(SelectionComponent::selection2)).apply(instance, SelectionComponent::new));

    public static final PacketCodec<ByteBuf, SelectionComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.optional(BlockPos.PACKET_CODEC),
            component -> component.selection1, PacketCodecs.optional(BlockPos.PACKET_CODEC), component -> component.selection2, SelectionComponent::new);

    public static final SelectionComponent EMPTY = new SelectionComponent(Optional.empty(), Optional.empty());

    public SelectionComponent of(Optional<BlockPos> selection, boolean isSelection2) {
        return new SelectionComponent(isSelection2 ? selection1 : selection, isSelection2 ? selection : selection2);
    }

    public static SelectionComponent get(ItemStack stack) {
        return stack.getOrDefault(AutomataComponents.SELECTION, SelectionComponent.EMPTY);
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
