package pro.piconico.automata.loader;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import pro.piconico.automata.Automata;

public class LoaderUtils {
    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static boolean isServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    public static void assertClient() {
        if (!isClient())
            throw new IllegalCallerException("Client-only code was run in a non-client environment");
    }

    public static void assertServer() {
        if (!isServer())
            throw new IllegalCallerException("Server-only code was run in a non-server environment");
    }

    public static void logError(String message, Function<String, ? extends RuntimeException> errorFactory) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            throw errorFactory.apply(message);
        }
        else {
            Automata.LOGGER.error(message);
        }
    }
}
