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

public record BotTeamComponent(UUID teamUuid) {
    public static final Codec<BotTeamComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
            Uuids.INT_STREAM_CODEC.fieldOf("team_uuid").forGetter(BotTeamComponent::teamUuid)) //
            .apply(instance, BotTeamComponent::new));

    public static final PacketCodec<ByteBuf, BotTeamComponent> PACKET_CODEC = PacketCodec.tuple( //
            Uuids.PACKET_CODEC, BotTeamComponent::teamUuid, //
            BotTeamComponent::new);

    public static Optional<UUID> get(ItemStack stack) {
        BotTeamComponent teamComponent = stack.get(AutomataComponents.BOT_TEAM);
        return Optional.ofNullable(teamComponent != null ? teamComponent.teamUuid : null);
    }

    public static boolean exists(ItemStack stack) {
        return get(stack).isPresent();
    }

    public static void set(ItemStack stack, Optional<UUID> teamUuid) {
        Optional<UUID> oldTeamUuid = get(stack);

        if (oldTeamUuid.equals(teamUuid))
            return;

        if (teamUuid.isEmpty()) {
            stack.remove(AutomataComponents.BOT_TEAM);
        }
        else {
            stack.set(AutomataComponents.BOT_TEAM, new BotTeamComponent(teamUuid.get()));
        }
    }
}
