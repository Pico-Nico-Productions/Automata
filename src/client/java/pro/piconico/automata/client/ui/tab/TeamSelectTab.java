package pro.piconico.automata.client.ui.tab;

import java.util.Optional;
import java.util.UUID;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.client.design.AutomataColors;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.client.ui.component.AutomataUIComponents;
import pro.piconico.automata.network.message.BotTeamSelectMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public class TeamSelectTab implements Tab<BotDeviceScreenHandler> {
    public static final TeamSelectTab INSTANCE = new TeamSelectTab();

    private TeamSelectTab() {
    }

    @Override
    public Text getName(BotDeviceScreenHandler handler) {
        return AutomataTexts.getBotDeviceTeamSelect();
    }

    @Override
    public ButtonComponent.Renderer getButtonRenderer(BotDeviceScreenHandler handler) {
        return AutomataClientTextures.BOT_DEVICE_TEAM_SELECT_ICON.getButtonRenderer();
    }

    @Override
    public void build(BotDeviceScreenHandler handler, FlowLayout parent) {
        parent.child(AutomataUIComponents.centerHeader(getName(handler)));

        if (handler.teams.isEmpty()) {
            FlowLayout infoRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
            infoRow.horizontalAlignment(HorizontalAlignment.CENTER);
            infoRow.child(UIComponents.label(AutomataTexts.getTeamsEmpty()));
            parent.child(infoRow);
            return;
        }

        Optional<UUID> teamUuid = handler.device.getTeamUuid();
        FlowLayout listContainer = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        listContainer.gap(BotDeviceScreenHandler.UI_SPACING);
        for (BotTeam team : handler.teams) {
            boolean isSelectedTeam = teamUuid.isPresent() && teamUuid.get().equals(team.UUID);

            FlowLayout teamRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
            teamRow.verticalAlignment(VerticalAlignment.CENTER);
            teamRow.padding(Insets.horizontal(BotDeviceScreenHandler.UI_SPACING));
            teamRow.surface(Surface.flat(AutomataColors.HOVERED).and(Surface.outline(isSelectedTeam ? AutomataColors.ACTIVE : AutomataColors.INACTIVE)));
            teamRow.cursorStyle(CursorStyle.HAND);
            teamRow.mouseDown().subscribe((click, doubled) -> {
                if (click.button() != 0)
                    return false;

                Optional<UUID> newTeamUuid = Optional.ofNullable(isSelectedTeam ? null : team.UUID);
                AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotTeamSelectMessage(newTeamUuid));

                return true;
            });
            listContainer.child(teamRow);

            LabelComponent nameLabel = UIComponents.label(Text.literal(team.getName()));
            nameLabel.horizontalSizing(Sizing.fill(65));
            nameLabel.cursorStyle(CursorStyle.HAND);
            teamRow.child(nameLabel);

            LabelComponent uuidLabel = UIComponents.label(Text.literal(team.UUID.toString().substring(0, 8)));
            uuidLabel.horizontalSizing(Sizing.fill(35));
            uuidLabel.horizontalTextAlignment(HorizontalAlignment.RIGHT);
            uuidLabel.cursorStyle(CursorStyle.HAND);
            teamRow.child(uuidLabel);
        }
        parent.child(listContainer);
    }
}
