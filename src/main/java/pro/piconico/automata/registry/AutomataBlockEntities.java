package pro.piconico.automata.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import pro.piconico.automata.Automata;
import pro.piconico.automata.block.entity.RoboportBlockEntity;

public class AutomataBlockEntities {
    public static final BlockEntityType<RoboportBlockEntity> ROBOPORT = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(Automata.MOD_ID, "roboport"), FabricBlockEntityTypeBuilder.create(RoboportBlockEntity::new, AutomataBlocks.ROBOPORT).build());

    public static void initialize() {
    }
}