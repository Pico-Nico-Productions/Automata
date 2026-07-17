package pro.piconico.automata.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.screen.RoboportScreenHandler;

public class AutomataScreenHandlers {
    public static final ScreenHandlerType<RoboportScreenHandler> ROBOPORT = register(AutomataRegistry.ROBOPORT, RoboportScreenHandler::new, BlockPos.PACKET_CODEC);

    private static <T extends ScreenHandler, D> ExtendedScreenHandlerType<T, D> register(String name, ExtendedScreenHandlerType.ExtendedFactory<T, D> factory, PacketCodec<? super RegistryByteBuf, D> packetCodec) {
        RegistryKey<ScreenHandlerType<?>> screenHandlerKey = AutomataRegistry.toRegistryKey(RegistryKeys.SCREEN_HANDLER, name);
        ExtendedScreenHandlerType<T, D> screenHandler = new ExtendedScreenHandlerType<>(factory, packetCodec);
        Registry.register(Registries.SCREEN_HANDLER, screenHandlerKey, screenHandler);
        
        return screenHandler;
    }

    public static void initialize() {
    }
}
