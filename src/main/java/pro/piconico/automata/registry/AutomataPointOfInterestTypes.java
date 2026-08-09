package pro.piconico.automata.registry;

import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import pro.piconico.automata.block.entity.RoboportBlockEntity;

public class AutomataPointOfInterestTypes {
    public static final RegistryKey<PointOfInterestType> ROBOPORT = register(AutomataRegistry.ROBOPORT, AutomataBlocks.ROBOPORT, 0, RoboportBlockEntity.CHUNK_RANGE + 1);

    private static RegistryKey<PointOfInterestType> register(String name, Block block, int ticketCount, int searchDistance) {
        RegistryKey<PointOfInterestType> pointOfInterestKey = AutomataRegistry.toRegistryKey(RegistryKeys.POINT_OF_INTEREST_TYPE, name);

        Set<BlockState> blockStates = PointOfInterestTypes.getStatesOfBlock(block);
        PointOfInterestTypes.register(Registries.POINT_OF_INTEREST_TYPE, pointOfInterestKey, blockStates, ticketCount, searchDistance);

        return pointOfInterestKey;
    }

    public static void initialize() {
    }
}
