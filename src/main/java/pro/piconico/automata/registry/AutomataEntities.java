package pro.piconico.automata.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import pro.piconico.automata.block.entity.RoboportBlockEntity;

public class AutomataEntities {
    public static final BlockEntityType<RoboportBlockEntity> ROBOPORT = register(AutomataRegistry.ROBOPORT, RoboportBlockEntity::new, AutomataBlocks.ROBOPORT);

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, FabricBlockEntityTypeBuilder.Factory<T> blockEntityFactory, Block block) {
        RegistryKey<BlockEntityType<?>> blockEntityKey = AutomataRegistry.toRegistryKey(RegistryKeys.BLOCK_ENTITY_TYPE, name);
        BlockEntityType<T> blockEntity = FabricBlockEntityTypeBuilder.create(blockEntityFactory, block).build();
        Registry.register(Registries.BLOCK_ENTITY_TYPE, blockEntityKey, blockEntity);
        
        return blockEntity;
    }

    public static void initialize() {
    }
}
