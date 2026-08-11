package pro.piconico.automata.datagen.lang;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import pro.piconico.automata.registry.AutomataBlocks;
import pro.piconico.automata.registry.AutomataItems;
import pro.piconico.automata.registry.AutomataTexts;

public class AutomataEnglishProvider extends FabricLanguageProvider {
    public AutomataEnglishProvider(FabricDataOutput dataOutput, CompletableFuture<WrapperLookup> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(AutomataTexts.DECONSTRUCTION_FAILED, "Must have 2 selections to deconstruct");
        translationBuilder.add(AutomataTexts.NETWORKS_REMOVED, "Removed %1$s network(s)");
        translationBuilder.add(AutomataTexts.JOBS_ADDED, "Added %1$s job(s)");
        translationBuilder.add(AutomataTexts.JOBS_REMOVED, "Removed %1$s job(s)");
        translationBuilder.add(AutomataTexts.SELECTED, "Selection %1$s: (%2$s)");

        translationBuilder.add(AutomataItems.COMMAND_TOOL, "Command Tool");
        translationBuilder.add(AutomataItems.CONSTRUCTION_BOT, "Construction Bot");

        AutomataLanguageGenerationUtils.AddBlockTranslation(translationBuilder, AutomataBlocks.ROBOPORT, "Roboport");
    }
}
