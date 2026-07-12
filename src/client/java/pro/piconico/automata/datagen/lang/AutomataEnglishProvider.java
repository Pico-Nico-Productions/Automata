package pro.piconico.automata.datagen.lang;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import pro.piconico.automata.registry.AutomataBlocks;

public class AutomataEnglishProvider extends FabricLanguageProvider {
    public AutomataEnglishProvider(FabricDataOutput dataOutput, CompletableFuture<WrapperLookup> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        AutomataLanguageGenerationUtils.AddBlockTranslation(translationBuilder, AutomataBlocks.ROBOPORT, "Roboport");
    }
}
