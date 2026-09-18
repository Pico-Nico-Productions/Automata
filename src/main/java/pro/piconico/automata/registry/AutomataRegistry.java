package pro.piconico.automata.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import pro.piconico.automata.Automata;

public class AutomataRegistry {
    public static final String AUTOMATOOL = "automatool";
    public static final String BOT = "bot";
    public static final String BOT_SYNC_S2C_PACKET = "bot_sync_s2c_packet";
    public static final String ROBOPORT = "roboport";
    public static final String SELECTION = "selection";

    public static final String BOT_DEVICE = BOT + "_device";
    public static final String BOT_JOB = BOT + "_job";
    public static final String BOT_TEAM = BOT + "_team";
    public static final String CONSTRUCTION_BOT = "construction_" + BOT;

    public static final String BLOCK_BOT_DEVICE = "block_" + BOT_DEVICE;
    public static final String BOT_JOB_PERSISTENT_STATE = BOT_JOB + "_persistent_state";
    public static final String BOT_TEAM_PERSISTENT_STATE = BOT_TEAM + "_persistent_state";
    public static final String DECONSTRUCTION_BOT_JOB = "deconstruction_" + BOT_JOB;
    public static final String ITEM_BOT_DEVICE = "item_" + BOT_DEVICE;

    public static Identifier id(String path) {
        return Identifier.of(Automata.MOD_ID, path);
    }

    public static Identifier packetId(String path) {
        return id("packet/" + path);
    }

    public static <T> RegistryKey<T> toRegistryKey(RegistryKey<Registry<T>> registryKey, String path) {
        return RegistryKey.of(registryKey, id(path));
    }

    public static String toTextTranslationKey(String name) {
        return "text." + Automata.MOD_ID + "." + name;
    }
}
