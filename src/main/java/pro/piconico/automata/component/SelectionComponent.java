package pro.piconico.automata.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import pro.piconico.automata.registry.AutomataComponents;

public record SelectionComponent(RegistryKey<World> worldKey, Optional<BlockPos> selection1, Optional<BlockPos> selection2) {
    public enum Selection {
        ONE, TWO;

        public int toInt() {
            return ordinal() + 1;
        }
    }

    public static final Codec<SelectionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
            RegistryKey.createCodec(RegistryKeys.WORLD).fieldOf("world_key").forGetter(SelectionComponent::worldKey), //
            BlockPos.CODEC.optionalFieldOf("selection1").forGetter(SelectionComponent::selection1), //
            BlockPos.CODEC.optionalFieldOf("selection2").forGetter(SelectionComponent::selection2)) //
            .apply(instance, SelectionComponent::new));

    public static final PacketCodec<ByteBuf, SelectionComponent> PACKET_CODEC = PacketCodec.tuple( //
            RegistryKey.createPacketCodec(RegistryKeys.WORLD), SelectionComponent::worldKey, //
            PacketCodecs.optional(BlockPos.PACKET_CODEC), component -> component.selection1, //
            PacketCodecs.optional(BlockPos.PACKET_CODEC), component -> component.selection2, //
            SelectionComponent::new);

    public SelectionComponent(World world, BlockPos selection1, BlockPos selection2) {
        this(world.getRegistryKey(), Optional.of(selection1), Optional.of(selection2));
    }

    public SelectionComponent(World world, BlockPos pos, Selection selection) {
        this(world.getRegistryKey(), Optional.ofNullable(selection == Selection.ONE ? pos : null),
                Optional.ofNullable(selection == Selection.TWO ? pos : null));
    }

    public Optional<ServerWorld> getWorld(MinecraftServer server) {
        return Optional.ofNullable(server.getWorld(worldKey));
    }

    public static Optional<SelectionComponent> get(ItemStack stack) {
        return Optional.ofNullable(stack.get(AutomataComponents.SELECTION));
    }

    public static boolean exists(ItemStack stack) {
        return get(stack).isPresent();
    }

    public static void set(ItemStack stack, Optional<SelectionComponent> selection) {
        Optional<SelectionComponent> oldSelection = get(stack);

        if (oldSelection.equals(selection))
            return;

        if (selection.filter(s -> s.selection1.isPresent() || s.selection2.isPresent()).isEmpty()) {
            stack.remove(AutomataComponents.SELECTION);
        }
        else {
            stack.set(AutomataComponents.SELECTION, selection.get());
        }
    }

    public List<BlockPos> toList() {
        ArrayList<BlockPos> selections = new ArrayList<>();
        selection1.ifPresent(s -> selections.add(s));
        selection2.ifPresent(s -> selections.add(s));

        return selections;
    }

    public int getSelectionCount() {
        return selection1.map(ignored -> 1).orElse(0) + selection2.map(ignored -> 1).orElse(0);
    }

    public Optional<Box> getSelectionBox() {
        if (getSelectionCount() < 2) {
            return Optional.empty();
        }

        return Optional.of(Box.enclosing(selection1.get(), selection2.get()));
    }
}
