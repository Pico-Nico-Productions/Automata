package pro.piconico.automata.datagen.loot_table;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import pro.piconico.automata.registry.AutomataBlocks;

public class AutomataBlockLootTableProvider extends FabricBlockLootTableProvider {
    public AutomataBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(AutomataBlocks.ROBOPORT);
    }
}