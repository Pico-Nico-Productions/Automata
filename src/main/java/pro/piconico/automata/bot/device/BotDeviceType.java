package pro.piconico.automata.bot.device;

import java.util.Optional;
import java.util.function.BiFunction;
import com.mojang.serialization.MapCodec;
import net.minecraft.entity.player.PlayerEntity;

public record BotDeviceType<IdT extends BotDevice.Id, BotDeviceT extends BotDevice<?>>(MapCodec<IdT> codec, BiFunction<PlayerEntity, IdT, Optional<BotDeviceT>> resolve) {
}
