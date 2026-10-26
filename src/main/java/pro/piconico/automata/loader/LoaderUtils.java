package pro.piconico.automata.loader;

import java.util.function.Function;
import net.fabricmc.loader.api.FabricLoader;
import pro.piconico.automata.Automata;

public class LoaderUtils {
    public static void logError(String message, Function<String, ? extends RuntimeException> errorFactory) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            throw errorFactory.apply(message);
        }
        else {
            Automata.LOGGER.error(message);
        }
    }
}
