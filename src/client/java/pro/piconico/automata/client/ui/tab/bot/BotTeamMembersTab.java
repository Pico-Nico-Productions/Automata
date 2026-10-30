package pro.piconico.automata.client.ui.tab.bot;

import java.util.Collection;
import java.util.List;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeamMember;
import pro.piconico.automata.client.registry.AutomataClientColors;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.client.ui.component.AutomataUIComponents;
import pro.piconico.automata.client.ui.tab.Tab;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public class BotTeamMembersTab implements Tab<BotDeviceScreenHandler> {
    public static final BotTeamMembersTab INSTANCE = new BotTeamMembersTab();
    public static final int ROW_NAME_FILL = 65;

    private BotTeamMembersTab() {
    }

    @Override
    public Text getName(BotDeviceScreenHandler handler) {
        return AutomataTexts.getBotTeamMembers();
    }

    @Override
    public ButtonComponent.Renderer getButtonRenderer(BotDeviceScreenHandler handler) {
        return AutomataClientTextures.BOT_TEAM_MEMBERS_ICON.getButtonRenderer();
    }

    @Override
    public void build(BotDeviceScreenHandler handler, FlowLayout parent) {
        parent.child(AutomataUIComponents.centerHeader(getName(handler)));

        Collection<BotTeamMember> members = handler.getTeam().map(team -> team.getMembers()).orElse(List.of());

        if (members.isEmpty()) {
            parent.child(AutomataUIComponents.centerBody(AutomataTexts.getBotTeamMembersEmpty()));
            return;
        }

        // TODO: Convert to scrollable layout
        FlowLayout listContainer = UIContainers.verticalFlow(Sizing.fill(), Sizing.content());
        listContainer.gap(BotDeviceScreenHandler.GAP);
        for (BotTeamMember member : members) {
            Color roleColor = AutomataClientColors.getRoleColor(member.getRole());

            LabelComponent nameLabel = UIComponents.label(Text.literal(member.getCachedName()));
            nameLabel.horizontalSizing(Sizing.fill(ROW_NAME_FILL));

            LabelComponent roleLabel = UIComponents.label(Text.literal(member.getRole().formatName()));
            roleLabel.horizontalTextAlignment(HorizontalAlignment.RIGHT);
            roleLabel.color(roleColor);
            roleLabel.horizontalSizing(Sizing.fill(100 - ROW_NAME_FILL));

            listContainer.child(AutomataUIComponents.clickableRow(AutomataClientColors.BUTTON_ACTIVE, roleColor, (click, doubled) -> {
                if (click.button() != 0)
                    return false;

                // TODO: Add role cycle logic

                return true;
            }, nameLabel, roleLabel));
        }
        parent.child(listContainer);
    }
}
