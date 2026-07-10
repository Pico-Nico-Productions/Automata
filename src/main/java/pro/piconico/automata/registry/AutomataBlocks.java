package pro.piconico.automata.registry;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import pro.piconico.automata.Automata;
import pro.piconico.automata.block.RoboportBlock;

public class AutomataBlocks {
    public static final Block ROBOPORT = register("roboport", key -> new RoboportBlock(AbstractBlock.Settings.create().registryKey(key).strength(3.0f)));

    private static Block register(String name, Function<RegistryKey<Block>, Block> factory) {
        Identifier id = Identifier.of(Automata.MOD_ID, name);

        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        Block block = factory.apply(blockKey);
        Registry.register(Registries.BLOCK, blockKey, block);
        
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        Registry.register(Registries.ITEM, itemKey, new BlockItem(block, new Item.Settings().registryKey(itemKey)));

        return block;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(ROBOPORT));
    }
}