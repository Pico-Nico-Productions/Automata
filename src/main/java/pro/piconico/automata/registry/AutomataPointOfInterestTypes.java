package pro.piconico.automata.registry;

import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

public class AutomataPointOfInterestTypes {
    public static final RegistryKey<PointOfInterestType> LOGISTIC_CHEST = register(AutomataRegistry.LOGISTIC_CHEST, AutomataBlocks.LOGISTIC_CHEST);
    public static final RegistryKey<PointOfInterestType> ROBOPORT = register(AutomataRegistry.ROBOPORT, AutomataBlocks.ROBOPORT);

    private static RegistryKey<PointOfInterestType> register(String name, Block block, int ticketCount, int searchDistance) {
        RegistryKey<PointOfInterestType> pointOfInterestKey = AutomataRegistry.toRegistryKey(RegistryKeys.POINT_OF_INTEREST_TYPE, name);

        Set<BlockState> blockStates = PointOfInterestTypes.getStatesOfBlock(block);
        PointOfInterestTypes.register(Registries.POINT_OF_INTEREST_TYPE, pointOfInterestKey, blockStates, ticketCount, searchDistance);

        return pointOfInterestKey;
    }

    private static RegistryKey<PointOfInterestType> register(String name, Block block) {
        return register(name, block, 0, 1);
    }

    public static void initialize() {
    }
}
