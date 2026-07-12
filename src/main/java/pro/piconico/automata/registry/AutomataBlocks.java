package pro.piconico.automata.registry;

import java.util.HashMap;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.Factory;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import pro.piconico.automata.block.RoboportBlock;
import pro.piconico.automata.block.entity.RoboportBlockEntity;

public class AutomataBlocks {
    private static final HashMap<Block, Item> blockToItem = new HashMap<>();
    private static final HashMap<Block, BlockEntityType<?>> blockToEntity = new HashMap<>();
    
    public static Block ROBOPORT = register("roboport", RoboportBlock::new, RoboportBlockEntity::new);

    private static Block register(String name, Function<Settings, Block> blockFactory) {
        RegistryKey<Block> blockKey = AutomataRegistry.toRegistryKey(RegistryKeys.BLOCK, name);
        Block block = blockFactory.apply(Settings.create().registryKey(blockKey));
        Registry.register(Registries.BLOCK, blockKey, block);

        RegistryKey<Item> itemKey = AutomataRegistry.toRegistryKey(RegistryKeys.ITEM, name);
        Item item = new BlockItem(block, new Item.Settings().registryKey(itemKey));
        blockToItem.put(block, item);
        Registry.register(Registries.ITEM, itemKey, item);

        return block;
    }

    private static Block register(String name, Function<Settings, Block> blockFactory, Factory<BlockEntity> blockEntityFactory) {
        Block block = register(name, blockFactory);

        RegistryKey<BlockEntityType<?>> blockEntityKey = AutomataRegistry.toRegistryKey(RegistryKeys.BLOCK_ENTITY_TYPE, name);
        BlockEntityType<?> blockEntity = FabricBlockEntityTypeBuilder.create(blockEntityFactory, block).build();
        blockToEntity.put(block, blockEntity);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, blockEntityKey, blockEntity);
        
        return block;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(toItem(ROBOPORT)));
    }

    public static Item toItem(Block block) {
        return blockToItem.get(block);
    }

    public static BlockEntityType<?> toEntity(Block block) {
        return blockToEntity.get(block);
    }
}