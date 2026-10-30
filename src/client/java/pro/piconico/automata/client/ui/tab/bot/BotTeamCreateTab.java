package pro.piconico.automata.client.ui.tab.bot;

import io.wispforest.owo.ui.component.ButtonComponent.Renderer;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.text.Text;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.client.ui.component.BotTeamSettingsCrudForm;
import pro.piconico.automata.client.ui.component.BotTeamSettingsCrudForm.Mutation;
import pro.piconico.automata.client.ui.tab.Tab;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public class BotTeamCreateTab implements Tab<BotDeviceScreenHandler> {
    public static final BotTeamCreateTab INSTANCE = new BotTeamCreateTab();

    private BotTeamCreateTab() {
    }

    @Override
    public Text getName(BotDeviceScreenHandler handler) {
        return AutomataTexts.getBotTeamAdd();
    }

    @Override
    public Renderer getButtonRenderer(BotDeviceScreenHandler handler) {
        return AutomataClientTextures.BOT_TEAM_CREATE_ICON.getButtonRenderer();
    }

    @Override
    public void build(BotDeviceScreenHandler handler, FlowLayout parent) {
        BotTeamSettingsCrudForm.build(handler, parent, getName(handler), Mutation.CREATE);
    }
}
