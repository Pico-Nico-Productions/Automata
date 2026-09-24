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
        translationBuilder.add(AutomataTexts.BLOCK_SELECTED_KEY, "Selection %1$s: (%2$s)");
        translationBuilder.add(AutomataTexts.BOT_DEVICE_HOME_KEY, "Home");
        translationBuilder.add(AutomataTexts.BOT_DEVICE_TEAM_ADD_KEY, "Team Creation");
        translationBuilder.add(AutomataTexts.BOT_DEVICE_TEAM_SELECT_KEY, "Team Selection");
        translationBuilder.add(AutomataTexts.BOT_DEVICE_TEAM_SETTINGS_KEY, "Team Settings");
        translationBuilder.add(AutomataTexts.CREATE_KEY, "Create");
        translationBuilder.add(AutomataTexts.DECONSTRUCTION_FAILED_KEY, "Must have 2 selections to deconstruct");
        translationBuilder.add(AutomataTexts.DELETE_KEY, "Delete");
        translationBuilder.add(AutomataTexts.JOBS_ADDED_KEY, "Added %1$s job(s)");
        translationBuilder.add(AutomataTexts.JOBS_REMOVED_KEY, "Removed %1$s job(s)");
        translationBuilder.add(AutomataTexts.NAME_KEY, "Name");
        translationBuilder.add(AutomataTexts.TEAM_KEY, "Team \"%1$s\" (%2$s)");
        translationBuilder.add(AutomataTexts.TEAM_ADDED_KEY, "Added team \"%1$s\" (%2$s)");
        translationBuilder.add(AutomataTexts.TEAM_EMPTY_KEY, "No team selected");
        translationBuilder.add(AutomataTexts.TEAM_REMOVED_KEY, "Removed team \"%1$s\" (%2$s)");
        translationBuilder.add(AutomataTexts.TEAM_MISSING_KEY, "No such team \"%1$s\"");
        translationBuilder.add(AutomataTexts.TEAM_NAME_INVALID_KEY, "Invalid team name \"%1$s\"");
        translationBuilder.add(AutomataTexts.TEAM_SELECTED_KEY, "Selected team \"%1$s\"");
        translationBuilder.add(AutomataTexts.TEAMS_EMPTY_KEY, "No teams");
        translationBuilder.add(AutomataTexts.UPDATE_KEY, "Update");

        translationBuilder.add(AutomataItems.AUTOMATOOL, "Automatool");
        translationBuilder.add(AutomataItems.CONSTRUCTION_BOT, "Construction Bot");

        AutomataLanguageGenerationUtils.AddBlockTranslation(translationBuilder, AutomataBlocks.LOGISTIC_CHEST, "Logistic Chest");
        AutomataLanguageGenerationUtils.AddBlockTranslation(translationBuilder, AutomataBlocks.ROBOPORT, "Roboport");
    }
}
