package pro.piconico.automata.client.ui.tab;

import io.wispforest.owo.ui.component.ButtonComponent.Renderer;
import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.network.message.BotTeamDeleteC2SMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public class TeamSettingsTab implements Tab<BotDeviceScreenHandler> {
    public static final TeamSettingsTab INSTANCE = new TeamSettingsTab();

    private TeamSettingsTab() {
    }

    @Override
    public Text getName(BotDeviceScreenHandler handler) {
        return AutomataTexts.getBotDeviceTeamSettings();
    }

    @Override
    public Renderer getButtonRenderer(BotDeviceScreenHandler handler) {
        return AutomataClientTextures.BOT_DEVICE_TEAM_SETTINGS_ICON.getButtonRenderer();
    }

    @Override
    public void build(BotDeviceScreenHandler handler, FlowLayout parent) {
        FlowLayout headerRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        headerRow.horizontalAlignment(HorizontalAlignment.CENTER);
        headerRow.child(UIComponents.label(getName(handler)).shadow(true));
        parent.child(headerRow);

        Optional<UUID> teamUuid = handler.botDevice.getTeamUuid();
        Optional<BotTeam> team = teamUuid.map(uuid -> handler.teams.stream().filter(t -> t.UUID.equals(uuid)).findAny()).orElse(Optional.empty());

        if (team.isEmpty()) {
            FlowLayout infoRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
            infoRow.horizontalAlignment(HorizontalAlignment.CENTER);
            infoRow.child(UIComponents.label(AutomataTexts.getTeamEmpty()));
            parent.child(infoRow);
            return;
        }

        FlowLayout nameRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        nameRow.verticalAlignment(VerticalAlignment.CENTER);
        nameRow.child(UIComponents.label(AutomataTexts.getName()));
        nameRow.child(UIComponents.textBox(Sizing.expand(), team.get().getName()));
        parent.child(nameRow);

        FlowLayout actionsRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        actionsRow.horizontalAlignment(HorizontalAlignment.CENTER);
        actionsRow.gap(BotDeviceScreenHandler.UI_SPACING);
        actionsRow.child(UIComponents.button(AutomataTexts.getDelete(), (button) -> {
            AutomataMessages.CHANNEL.clientHandle().send(new BotTeamDeleteC2SMessage(team.get().UUID));
        }));
        parent.child(actionsRow);
    }
}
