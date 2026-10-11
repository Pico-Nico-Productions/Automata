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
import pro.piconico.automata.item.AutomatoolItem;
import pro.piconico.automata.item.BotItem;
import pro.piconico.automata.item.ConstructionBotItem;

public class AutomataItems {
    public static final Item AUTOMATOOL = register(AutomataRegistry.AUTOMATOOL, AutomatoolItem::new);
    public static final BotItem CONSTRUCTION_BOT = register(AutomataRegistry.CONSTRUCTION_BOT, ConstructionBotItem::new);

    private static <T extends Item> T register(String name, Function<Settings, T> itemFactory) {
        RegistryKey<Item> itemKey = AutomataRegistry.toRegistryKey(RegistryKeys.ITEM, name);
        T item = itemFactory.apply(new Item.Settings().registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
        AutomatoolItem.initialize();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(AUTOMATOOL));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(CONSTRUCTION_BOT));
    }
}
