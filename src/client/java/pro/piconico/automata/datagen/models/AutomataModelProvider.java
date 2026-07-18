package pro.piconico.automata.datagen.models;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.TexturedModel;
import pro.piconico.automata.registry.AutomataBlocks;
import pro.piconico.automata.registry.AutomataItems;

public class AutomataModelProvider extends FabricModelProvider {
    public AutomataModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(AutomataItems.COMMAND_TOOL, Models.GENERATED);
        itemModelGenerator.register(AutomataItems.CONSTRUCTION_BOT, Models.GENERATED);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSingleton(AutomataBlocks.ROBOPORT, TexturedModel.CUBE_TOP);
    }
}
