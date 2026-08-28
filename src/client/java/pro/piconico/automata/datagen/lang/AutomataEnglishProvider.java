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
        translationBuilder.add(AutomataTexts.BLOCK_SELECTED, "Selection %1$s: (%2$s)");
        translationBuilder.add(AutomataTexts.DECONSTRUCTION_FAILED, "Must have 2 selections to deconstruct");
        translationBuilder.add(AutomataTexts.JOBS_ADDED, "Added %1$s job(s)");
        translationBuilder.add(AutomataTexts.JOBS_REMOVED, "Removed %1$s job(s)");
        translationBuilder.add(AutomataTexts.TEAM, "Team \"%1$s\" (%2$s)");
        translationBuilder.add(AutomataTexts.TEAM_CREATED, "Created team \"%1$s\" (%2$s)");
        translationBuilder.add(AutomataTexts.TEAM_DELETED, "Deleted team \"%1$s\" (%2$s)");
        translationBuilder.add(AutomataTexts.TEAM_MISSING, "No such team \"%1$s\"");
        translationBuilder.add(AutomataTexts.TEAM_NAME_INVALID, "Invalid team name \"%1$s\"");
        translationBuilder.add(AutomataTexts.TEAM_SELECTED, "Selected team \"%1$s\"");
        translationBuilder.add(AutomataTexts.TEAMS_EMPTY, "Teams empty");

        translationBuilder.add(AutomataItems.COMMAND_TOOL, "Command Tool");
        translationBuilder.add(AutomataItems.CONSTRUCTION_BOT, "Construction Bot");

        AutomataLanguageGenerationUtils.AddBlockTranslation(translationBuilder, AutomataBlocks.ROBOPORT, "Roboport");
    }
}
