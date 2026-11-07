package pro.piconico.automata.component;

import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.registry.AutomataComponents;

public record BlueprintComponent(Map<BlockPos, BlockState> blocks) {
    public static final Codec<BlueprintComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
            Codec.unboundedMap(BlockPos.CODEC, BlockState.CODEC).fieldOf("blocks").forGetter(BlueprintComponent::blocks)) //
            .apply(instance, BlueprintComponent::new));

    public static final PacketCodec<ByteBuf, BlueprintComponent> PACKET_CODEC = PacketCodec.tuple( //
            PacketCodecs.map(HashMap::new, BlockPos.PACKET_CODEC, PacketCodecs.codec(BlockState.CODEC)), BlueprintComponent::blocks, //
            BlueprintComponent::new);

    public static Map<BlockPos, BlockState> get(ItemStack stack) {
        BlueprintComponent blueprintComponent = stack.get(AutomataComponents.BLUEPRINT_COMPONENT);
        return blueprintComponent != null ? blueprintComponent.blocks : Map.of();
    }

    public static void set(ItemStack stack, Map<BlockPos, BlockState> blocks) {
        Map<BlockPos, BlockState> oldBlocks = get(stack);

        if (oldBlocks.equals(blocks))
            return;

        if (blocks.isEmpty()) {
            stack.remove(AutomataComponents.BLUEPRINT_COMPONENT);
        }
        else {
            stack.set(AutomataComponents.BLUEPRINT_COMPONENT, new BlueprintComponent(blocks));
        }
    }
}
