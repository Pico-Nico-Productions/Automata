package pro.piconico.automata.client.ui.tab;

import io.wispforest.owo.ui.component.ButtonComponent.Renderer;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.text.Text;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.client.ui.component.BotTeamSettingsCrudForm;
import pro.piconico.automata.client.ui.component.BotTeamSettingsCrudForm.Mutation;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public class TeamCreateTab implements Tab<BotDeviceScreenHandler> {
    public static final TeamCreateTab INSTANCE = new TeamCreateTab();

    private TeamCreateTab() {
    }

    @Override
    public Text getName(BotDeviceScreenHandler handler) {
        return AutomataTexts.getBotDeviceTeamAdd();
    }

    @Override
    public Renderer getButtonRenderer(BotDeviceScreenHandler handler) {
        return AutomataClientTextures.BOT_DEVICE_TEAM_CREATE_ICON.getButtonRenderer();
    }

    @Override
    public void build(BotDeviceScreenHandler handler, FlowLayout parent) {
        BotTeamSettingsCrudForm.build(handler, parent, getName(handler), Mutation.CREATE);
    }
}
