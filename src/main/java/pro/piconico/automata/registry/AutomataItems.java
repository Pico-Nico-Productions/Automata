package pro.piconico.automata.registry;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import pro.piconico.automata.item.ConstructionBotItem;

public class AutomataItems {
    public static final Item CONSTRUCTION_BOT = register("construction_bot", ConstructionBotItem::new);

    private static Item register(String name, Function<Settings, Item> itemFactory) {
        RegistryKey<Item> itemKey = AutomataRegistry.toRegistryKey(RegistryKeys.ITEM, name);
        Item item = itemFactory.apply(new Item.Settings().registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(CONSTRUCTION_BOT));
    }
}
