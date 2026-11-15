package pro.piconico.automata.component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.registry.AutomataComponents;

public record BlueprintComponent(Map<BlockPos, BlueprintBlockState> blockStates) {
    public static record BlueprintBlockState(BlockPos relativePos, BlockState state, Optional<NbtCompound> entityData) {
        public static final Codec<BlueprintBlockState> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
                BlockPos.CODEC.fieldOf("relative_pos").forGetter(BlueprintBlockState::relativePos), //
                BlockState.CODEC.fieldOf("state").forGetter(BlueprintBlockState::state), //
                NbtCompound.CODEC.optionalFieldOf("entityData").forGetter(BlueprintBlockState::entityData) //
        ).apply(instance, BlueprintBlockState::new));

        public static final PacketCodec<ByteBuf, BlueprintBlockState> PACKET_CODEC = PacketCodecs.codec(CODEC);
    }

    public static final Codec<BlueprintComponent> CODEC = BlueprintBlockState.CODEC.listOf().xmap(BlueprintComponent::new,
            blueprint -> blueprint.blockStates.values().stream().toList());

    public static final PacketCodec<ByteBuf, BlueprintComponent> PACKET_CODEC = PacketCodecs.codec(CODEC);

    public BlueprintComponent(Collection<BlueprintBlockState> blockStates) {
        this(blockStates.stream().collect(Collectors.toMap(b -> b.relativePos, b -> b)));
    }

    public static Map<BlockPos, BlueprintBlockState> get(ItemStack stack) {
        BlueprintComponent blueprintComponent = stack.get(AutomataComponents.BLUEPRINT_COMPONENT);
        return blueprintComponent != null ? blueprintComponent.blockStates : Map.of();
    }

    public static boolean exists(ItemStack stack) {
        return !get(stack).isEmpty();
    }

    public static void set(ItemStack stack, Collection<BlueprintBlockState> blockStates) {
        Map<BlockPos, BlueprintBlockState> oldBlockStates = get(stack);
        if (oldBlockStates.values().equals(blockStates))
            return;

        if (blockStates.isEmpty()) {
            stack.remove(AutomataComponents.BLUEPRINT_COMPONENT);
        }
        else {
            stack.set(AutomataComponents.BLUEPRINT_COMPONENT, new BlueprintComponent(blockStates));
        }
    }
}
