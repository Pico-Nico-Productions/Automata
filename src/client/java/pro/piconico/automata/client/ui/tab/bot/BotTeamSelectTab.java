package pro.piconico.automata.client.ui.tab.bot;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.client.registry.AutomataClientColors;
import pro.piconico.automata.client.ui.component.AutomataUIComponents;
import pro.piconico.automata.client.ui.tab.Tab;
import pro.piconico.automata.network.message.BotDeviceTeamChangeMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public class BotTeamSelectTab implements Tab<BotDeviceScreenHandler> {
    public static final BotTeamSelectTab INSTANCE = new BotTeamSelectTab();
    public static final int ROW_NAME_FILL = 65;

    private BotTeamSelectTab() {
    }

    @Override
    public Text getName(BotDeviceScreenHandler handler) {
        return AutomataTexts.getBotTeamSelect();
    }

    @Override
    public ButtonComponent.Renderer getButtonRenderer(BotDeviceScreenHandler handler) {
        return AutomataClientTextures.BOT_TEAM_SELECT_ICON.getButtonRenderer();
    }

    @Override
    public void build(BotDeviceScreenHandler handler, FlowLayout parent) {
        parent.child(AutomataUIComponents.centerHeader(getName(handler)));

        if (handler.teams.isEmpty()) {
            parent.child(AutomataUIComponents.centerBody(AutomataTexts.getBotTeamsEmpty()));
            return;
        }

        Optional<UUID> teamUuid = handler.device.getTeamUuid();
        FlowLayout listContainer = UIContainers.verticalFlow(Sizing.fill(), Sizing.content());
        listContainer.gap(BotDeviceScreenHandler.GAP);
        for (BotTeam team : handler.teams) {
            boolean isSelectedTeam = teamUuid.isPresent() && teamUuid.get().equals(team.uuid);
            Color outlineColor = isSelectedTeam ? AutomataClientColors.BUTTON_OUTLINE : AutomataClientColors.BUTTON_INACTIVE;

            LabelComponent nameLabel = UIComponents.label(Text.literal(team.getName()));
            nameLabel.horizontalSizing(Sizing.fill(ROW_NAME_FILL));

            LabelComponent uuidLabel = UIComponents.label(Text.literal(team.getTruncatedUuid()));
            uuidLabel.horizontalTextAlignment(HorizontalAlignment.RIGHT);
            uuidLabel.horizontalSizing(Sizing.fill(100 - ROW_NAME_FILL));

            listContainer.child(AutomataUIComponents.clickableRow(AutomataClientColors.BUTTON_ACTIVE, outlineColor, (click, doubled) -> {
                if (click.button() != 0)
                    return false;

                Optional<UUID> newTeamUuid = Optional.ofNullable(isSelectedTeam ? null : team.uuid);
                AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotDeviceTeamChangeMessage(handler.device.getDeviceId(), newTeamUuid));

                return true;
            }, nameLabel, uuidLabel));
        }
        parent.child(listContainer);
    }
}
