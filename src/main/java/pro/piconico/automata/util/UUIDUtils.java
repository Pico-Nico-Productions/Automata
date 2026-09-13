package pro.piconico.automata.util;

import java.util.Optional;
import java.util.UUID;

public class UUIDUtils {
    public static Optional<UUID> fromString(String string) {
        try {
            return Optional.of(UUID.fromString(string));
        }
        catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
