package pro.piconico.automata.registry;

import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import pro.piconico.automata.block.LogisticChestBlock;
import pro.piconico.automata.block.RoboportBlock;

public class AutomataBlocks {
    public static final Block LOGISTIC_CHEST = register(AutomataRegistry.LOGISTIC_CHEST, LogisticChestBlock::new, Blocks.CHEST);
    public static final Block ROBOPORT = register(AutomataRegistry.ROBOPORT, RoboportBlock::new);

    private static Block register(String name, Function<Settings, Block> blockFactory, Settings baseSettings) {
        RegistryKey<Block> blockKey = AutomataRegistry.toRegistryKey(RegistryKeys.BLOCK, name);
        Block block = blockFactory.apply(baseSettings.registryKey(blockKey));
        Registry.register(Registries.BLOCK, blockKey, block);

        RegistryKey<Item> itemKey = AutomataRegistry.toRegistryKey(RegistryKeys.ITEM, name);
        Item item = new BlockItem(block, new Item.Settings().registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);

        return block;
    }

    private static Block register(String name, Function<Settings, Block> blockFactory, Block baseBlock) {
        return register(name, blockFactory, Settings.copy(baseBlock));
    }

    private static Block register(String name, Function<Settings, Block> blockFactory) {
        return register(name, blockFactory, Settings.create());
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(LOGISTIC_CHEST.asItem()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(ROBOPORT.asItem()));
    }
}