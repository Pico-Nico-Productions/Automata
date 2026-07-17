package pro.piconico.automata.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import pro.piconico.automata.datagen.lang.AutomataEnglishProvider;
import pro.piconico.automata.datagen.loot_table.AutomataBlockLootTableProvider;
import pro.piconico.automata.datagen.models.AutomataModelProvider;

public class AutomataDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack automataPack = fabricDataGenerator.createPack();

		automataPack.addProvider(AutomataModelProvider::new);
		automataPack.addProvider(AutomataBlockLootTableProvider::new);

        automataPack.addProvider(AutomataEnglishProvider::new);
	}
}
