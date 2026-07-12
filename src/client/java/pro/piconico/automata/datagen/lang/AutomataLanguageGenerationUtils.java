package pro.piconico.automata.datagen.lang;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;
import net.minecraft.block.Block;
import pro.piconico.automata.registry.AutomataBlocks;

public class AutomataLanguageGenerationUtils {
    public static void AddBlockTranslation(TranslationBuilder translationBuilder, Block block, String translation) {
        translationBuilder.add(block, translation);
        translationBuilder.add(AutomataBlocks.toItem(block), translation);
    }
}
