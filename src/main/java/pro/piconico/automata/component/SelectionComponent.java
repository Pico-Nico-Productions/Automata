package pro.piconico.automata.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import pro.piconico.automata.registry.AutomataComponents;

public record SelectionComponent(Identifier worldId, Optional<BlockPos> selection1, Optional<BlockPos> selection2) {
    public static final Codec<SelectionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
            Identifier.CODEC.fieldOf("worldId").forGetter(SelectionComponent::worldId), //
            BlockPos.CODEC.optionalFieldOf("selection1").forGetter(SelectionComponent::selection1), //
            BlockPos.CODEC.optionalFieldOf("selection2").forGetter(SelectionComponent::selection2)) //
            .apply(instance, SelectionComponent::new));

    public static final PacketCodec<ByteBuf, SelectionComponent> PACKET_CODEC = PacketCodec.tuple( //
            Identifier.PACKET_CODEC, SelectionComponent::worldId, //
            PacketCodecs.optional(BlockPos.PACKET_CODEC), component -> component.selection1, //
            PacketCodecs.optional(BlockPos.PACKET_CODEC), component -> component.selection2, //
            SelectionComponent::new);

    public SelectionComponent(World world, BlockPos selection1, BlockPos selection2) {
        this(world.getRegistryKey().getValue(), Optional.of(selection1), Optional.of(selection2));
    }

    public SelectionComponent(World world, BlockPos selection, boolean isSelection2) {
        this(world.getRegistryKey().getValue(), Optional.ofNullable(isSelection2 ? null : selection), Optional.ofNullable(isSelection2 ? selection : null));
    }

    public static Optional<SelectionComponent> get(ItemStack stack) {
        return Optional.ofNullable(stack.get(AutomataComponents.SELECTION));
    }

    public SelectionComponent copyWith(BlockPos selection, boolean isSelection2) {
        return new SelectionComponent(worldId, isSelection2 ? selection1 : Optional.of(selection), isSelection2 ? Optional.of(selection) : selection2);
    }

    public static SelectionComponent getAndCopyWithOrElse(ItemStack stack, World world, BlockPos selection, boolean isSelection2) {
        Optional<SelectionComponent> original = get(stack);

        if (original.isPresent())
            return original.get().copyWith(selection, isSelection2);

        return new SelectionComponent(world, selection, isSelection2);
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
