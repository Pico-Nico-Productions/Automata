package pro.piconico.automata.datagen.lang;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import pro.piconico.automata.registry.AutomataBlocks;
import pro.piconico.automata.registry.AutomataEntities;
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
        translationBuilder.add(AutomataTexts.BOT_JOBS_ADDED_KEY, "Added %1$s job(s)");
        translationBuilder.add(AutomataTexts.BOT_TEAM_EMPTY_KEY, "No team selected");
        translationBuilder.add(AutomataTexts.BOT_JOBS_REMOVED_KEY, "Removed %1$s job(s)");
        translationBuilder.add(AutomataTexts.BOT_TEAM_ADD_KEY, "Team Creation");
        translationBuilder.add(AutomataTexts.BOT_TEAM_ADD_MEMBER_KEY, "Add Member");
        translationBuilder.add(AutomataTexts.BOT_TEAM_CYCLE_ROLE_KEY, "Cycle Role");
        translationBuilder.add(AutomataTexts.BOT_TEAM_MEMBERS_EMPTY_KEY, "No members");
        translationBuilder.add(AutomataTexts.BOT_TEAM_MEMBERS_KEY, "Team Members");
        translationBuilder.add(AutomataTexts.BOT_TEAM_NON_MEMBERS_KEY, "Non-Members");
        translationBuilder.add(AutomataTexts.BOT_TEAM_PERMISSION_MISSING_KEY, "%1$s doesn't have permission to do that on \"%2$s\" (%3$s)");
        translationBuilder.add(AutomataTexts.BOT_TEAM_REMOVE_MEMBER_KEY, "Remove Member");
        translationBuilder.add(AutomataTexts.BOT_TEAM_SELECT_KEY, "Team Selection");
        translationBuilder.add(AutomataTexts.BOT_TEAM_SETTINGS_KEY, "Team Settings");
        translationBuilder.add(AutomataTexts.BOT_TEAMS_EMPTY_KEY, "No teams");
        translationBuilder.add(AutomataTexts.CREATE_KEY, "Create");
        translationBuilder.add(AutomataTexts.DELETE_KEY, "Delete");
        translationBuilder.add(AutomataTexts.DELIMITER_KEY, " - ");
        translationBuilder.add(AutomataTexts.MOUSE_LEFT_KEY, "Left Click");
        translationBuilder.add(AutomataTexts.MOUSE_RIGHT_KEY, "Right Click");
        translationBuilder.add(AutomataTexts.NAME_KEY, "Name");
        translationBuilder.add(AutomataTexts.SELECTION_INVALID, "Must have %1$s selection(s) to do that");
        translationBuilder.add(AutomataTexts.UPDATE_KEY, "Update");

        translationBuilder.add(AutomataItems.AUTOMATOOL, "Automatool");
        translationBuilder.add(AutomataItems.CONSTRUCTION_BOT, "Construction Bot");

        AutomataLanguageGenerationUtils.AddBlockTranslation(translationBuilder, AutomataBlocks.LOGISTIC_CHEST, "Logistic Chest");
        AutomataLanguageGenerationUtils.AddBlockTranslation(translationBuilder, AutomataBlocks.ROBOPORT, "Roboport");

        translationBuilder.add(AutomataEntities.CONSTRUCTION_BOT, "Construction Bot");
    }
}
