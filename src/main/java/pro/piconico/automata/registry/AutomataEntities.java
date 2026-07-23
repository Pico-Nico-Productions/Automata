package pro.piconico.automata.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.EntityType.EntityFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import pro.piconico.automata.block.entity.RoboportBlockEntity;
import pro.piconico.automata.entity.ConstructionBotEntity;

public class AutomataEntities {
    public static final EntityType<ConstructionBotEntity> CONSTRUCTION_BOT = register(AutomataRegistry.CONSTRUCTION_BOT, ConstructionBotEntity::new, ConstructionBotEntity.createBeeAttributes());

    public static final BlockEntityType<RoboportBlockEntity> ROBOPORT = register(AutomataRegistry.ROBOPORT, RoboportBlockEntity::new, AutomataBlocks.ROBOPORT);

    private static <T extends Entity> EntityType<T> register(String name, EntityFactory<T> entityFactory, DefaultAttributeContainer.Builder builder) {
        RegistryKey<EntityType<?>> entityKey = AutomataRegistry.toRegistryKey(RegistryKeys.ENTITY_TYPE, name);
        EntityType<T> entity = EntityType.Builder.create(entityFactory, SpawnGroup.MISC).build(entityKey);
        Registry.register(Registries.ENTITY_TYPE, entityKey, entity);

        FabricDefaultAttributeRegistry.register((EntityType<? extends LivingEntity>)entity, builder);

        return entity;
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, FabricBlockEntityTypeBuilder.Factory<T> blockEntityFactory, Block block) {
        RegistryKey<BlockEntityType<?>> blockEntityKey = AutomataRegistry.toRegistryKey(RegistryKeys.BLOCK_ENTITY_TYPE, name);
        BlockEntityType<T> blockEntity = FabricBlockEntityTypeBuilder.create(blockEntityFactory, block).build();
        Registry.register(Registries.BLOCK_ENTITY_TYPE, blockEntityKey, blockEntity);
        
        return blockEntity;
    }

    public static void initialize() {
    }
}
