package pro.piconico.automata.client.ui.tab.bot;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeamMember;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamRole;
import pro.piconico.automata.client.input.MouseButton;
import pro.piconico.automata.client.registry.AutomataClientColors;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.client.ui.component.AutomataUIComponents;
import pro.piconico.automata.client.ui.tab.Tab;
import pro.piconico.automata.network.message.BotTeamMemberRoleChangeC2SMessage;
import pro.piconico.automata.registry.AutomataMessages;
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

        Optional<BotTeam> team = handler.getTeam();

        if (team.isEmpty()) {
            parent.child(AutomataUIComponents.centerBody(AutomataTexts.getBotTeamEmpty()));
            return;
        }

        Collection<BotTeamMember> members = team.get().getMembers();

        if (members.isEmpty()) {
            parent.child(AutomataUIComponents.centerBody(AutomataTexts.getBotTeamMembersEmpty()));
            return;
        }

        FlowLayout listContainer = UIContainers.verticalFlow(Sizing.fill(), Sizing.content());
        listContainer.gap(BotDeviceScreenHandler.GAP);

        for (BotTeamMember member : members) {
            Color roleColor = AutomataClientColors.getRoleColor(member.getRole());

            LabelComponent nameLabel = UIComponents.label(Text.literal(member.getCachedName()));
            nameLabel.horizontalSizing(Sizing.fill(ROW_NAME_FILL));

            LabelComponent roleLabel = UIComponents.label(Text.literal(member.getRole().toSentenceCase()));
            roleLabel.horizontalTextAlignment(HorizontalAlignment.RIGHT);
            roleLabel.color(roleColor);
            roleLabel.horizontalSizing(Sizing.fill(100 - ROW_NAME_FILL));

            boolean canChangeRole = BotTeamMemberRoleChangeC2SMessage.canChangeRole(handler.player(), team.get(), member.uuid);
            Color color = AutomataClientColors.BUTTON_ACTIVE;
            Color hoverColor = canChangeRole ? AutomataClientColors.BUTTON_HOVERED : color;
            FlowLayout row = AutomataUIComponents.clickableRow(color, roleColor, hoverColor, (click, doubled) -> {
                if (!canChangeRole)
                    return false;

                if (click.button() == MouseButton.LEFT.button) {
                    BotTeamRole newRole = BotTeamRole.values()[(member.getRole().ordinal() + 1) % BotTeamRole.values().length];
                    AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotTeamMemberRoleChangeC2SMessage(team.get().uuid, member.uuid, newRole));
                }
                else if (click.button() == MouseButton.RIGHT.button) {
                    AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotTeamMemberRoleChangeC2SMessage(team.get().uuid, member.uuid));
                }
                else
                    return false;

                return true;
            }, nameLabel, roleLabel);
            Style style = canChangeRole ? AutomataTexts.AFFIRMATIVE : AutomataTexts.DISMISSIVE;
            Text cycleRole = AutomataTexts.getMouseLeft().append(AutomataTexts.getDelimiter()).append(AutomataTexts.getBotTeamCycleRole()).setStyle(style);
            Text removeMember = AutomataTexts.getMouseRight().append(AutomataTexts.getDelimiter()).append(AutomataTexts.getBotTeamRemoveMember())
                    .setStyle(style);
            row.tooltip(List.of(cycleRole, removeMember));
            listContainer.child(row);
        }

        List<PlayerListEntry> nonMemberList = ((ClientPlayerEntity)handler.player()).networkHandler.getPlayerList().stream()
                .filter(playerEntry -> members.stream().noneMatch(member -> member.uuid.equals(playerEntry.getProfile().id()))).toList();
        if (!nonMemberList.isEmpty() && team.get().hasMemberWith(handler.player(), BotTeamPermission.CHANGE_MEMBER_ROLE)) {
            listContainer.child(AutomataUIComponents.centerBody(AutomataTexts.getBotTeamNonMembers()));
            for (PlayerListEntry playerEntry : nonMemberList) {
                LabelComponent nameLabel = UIComponents.label(Text.literal(playerEntry.getProfile().name()));
                nameLabel.horizontalSizing(Sizing.fill());

                FlowLayout row = AutomataUIComponents.clickableRow((click, doubled) -> {
                    if (click.button() != MouseButton.LEFT.button)
                        return false;

                    AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle()
                            .send(new BotTeamMemberRoleChangeC2SMessage(team.get().uuid, playerEntry.getProfile().id(), BotTeamRole.USER));

                    return true;
                }, nameLabel);
                row.tooltip(AutomataTexts.getBotTeamAddMember().setStyle(AutomataTexts.AFFIRMATIVE));
                listContainer.child(row);
            }
        }

        ScrollContainer<FlowLayout> scrollContainer = UIContainers.verticalScroll(Sizing.fill(), Sizing.fill(), listContainer);
        parent.child(scrollContainer);
    }
}
