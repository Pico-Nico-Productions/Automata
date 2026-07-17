package pro.piconico.automata.registry;

import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import pro.piconico.automata.block.RoboportBlock;

public class AutomataBlocks {
    public static Block ROBOPORT = register(AutomataRegistry.ROBOPORT, RoboportBlock::new);

    private static Block register(String name, Function<Settings, Block> blockFactory) {
        RegistryKey<Block> blockKey = AutomataRegistry.toRegistryKey(RegistryKeys.BLOCK, name);
        Block block = blockFactory.apply(Settings.create().registryKey(blockKey));
        Registry.register(Registries.BLOCK, blockKey, block);

        RegistryKey<Item> itemKey = AutomataRegistry.toRegistryKey(RegistryKeys.ITEM, name);
        Item item = new BlockItem(block, new Item.Settings().registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);

        return block;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(ROBOPORT.asItem()));
    }
}