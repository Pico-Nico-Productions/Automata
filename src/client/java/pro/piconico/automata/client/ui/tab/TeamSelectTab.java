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
        FlowLayout headerRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        headerRow.verticalAlignment(VerticalAlignment.CENTER);
        headerRow.child(UIComponents.label(getName(handler)).shadow(true));
        headerRow.child(UIContainers.horizontalFlow(Sizing.expand(), Sizing.fixed(0)));
        headerRow.child(UIComponents.label(Text.literal(Integer.toString(handler.teams.size()))).shadow(true));
        parent.child(headerRow);

        Optional<UUID> teamUuid = handler.botDevice.getTeamUuid();
        FlowLayout listContainer = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        listContainer.gap(BotDeviceScreenHandler.UI_SPACING);
        for (BotTeam team : handler.teams) {
            boolean isSelectedTeam = teamUuid.isPresent() && teamUuid.get().equals(team.UUID);

            FlowLayout teamRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
            teamRow.verticalAlignment(VerticalAlignment.CENTER);
            teamRow.padding(Insets.horizontal(BotDeviceScreenHandler.UI_SPACING));
            teamRow.surface(Surface.flat(AutomataColors.HOVERED).and(Surface.outline(isSelectedTeam ? AutomataColors.ACTIVE : AutomataColors.INACTIVE)));

            LabelComponent nameLabel = UIComponents.label(Text.literal(team.getName()));
            nameLabel.horizontalSizing(Sizing.fill(65));
            teamRow.child(nameLabel);

            LabelComponent uuidLabel = UIComponents.label(Text.literal(team.UUID.toString().substring(0, 8)));
            uuidLabel.horizontalSizing(Sizing.fill(35));
            uuidLabel.horizontalTextAlignment(HorizontalAlignment.RIGHT);
            teamRow.child(uuidLabel);

            if (!isSelectedTeam) {
                teamRow.cursorStyle(CursorStyle.HAND);
                nameLabel.cursorStyle(CursorStyle.HAND);
                uuidLabel.cursorStyle(CursorStyle.HAND);

                teamRow.mouseDown().subscribe((click, doubled) -> {
                    if (click.button() != 0)
                        return false;

                    AutomataMessages.CHANNEL.clientHandle().send(new BotTeamSelectMessage(Optional.of(team.UUID)));

                    return true;
                });
            }

            listContainer.child(teamRow);
        }
        parent.child(listContainer);
    }
}
