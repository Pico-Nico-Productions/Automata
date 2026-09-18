package pro.piconico.automata.client.gui.screen;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import io.wispforest.owo.ui.base.BaseOwoContainerScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.client.design.AutomataColors;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.network.message.BotTeamSelectMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.screen.slot.DisableableSlot;

public abstract class BotDeviceScreen<HandlerT extends BotDeviceScreenHandler> extends BaseOwoContainerScreen<FlowLayout, HandlerT> {
    protected enum Tab {
        HOME(deviceScreen -> deviceScreen.buildHomeTab(), AutomataTexts.getBotDeviceHome()), //
        TEAM_SELECT(deviceScreen -> deviceScreen.buildTeamSelectTab(), AutomataTexts.getBotDeviceTeamSelect());

        private final Consumer<BotDeviceScreen<?>> build;
        private final Text text;

        private Tab(Consumer<BotDeviceScreen<?>> build, Text text) {
            this.text = text;
            this.build = build;
        }

        private void build(BotDeviceScreen<?> deviceScreen) {
            deviceScreen.selectedTab = this;

            deviceScreen.body.clearChildren();
            build.accept(deviceScreen);

            boolean slotEnabled = deviceScreen.selectedTab == Tab.HOME;
            for (Slot slot : deviceScreen.getScreenHandler().slots) {
                ((DisableableSlot)slot).setEnabled(slotEnabled);
            }
        }

        private ButtonComponent buildButton(BotDeviceScreen<?> deviceScreen) {
            Consumer<ButtonComponent> onPress = button -> {
                build(deviceScreen);
            };
            ButtonComponent button = AutomataClientTextures.BOT_DEVICE_ICONS.toButtonComponent(ordinal(), Text.empty(), onPress);
            button.tooltip(text);

            return button;
        }
    }

    protected FlowLayout body;
    protected Tab selectedTab;

    public BotDeviceScreen(HandlerT handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    protected void buildSlot(int x, int y) {
        Sizing slotSize = Sizing.fixed(BotDeviceScreenHandler.SLOT_DELTA);
        ParentUIComponent parent = UIContainers.horizontalFlow(slotSize, slotSize).surface(Surface.PANEL_INSET);
        body.child(parent.positioning(Positioning.absolute(x - BotDeviceScreenHandler.SLOT_SIZE / 2, y - BotDeviceScreenHandler.SLOT_SIZE / 2)));
    }

    public void rebuildTab() {
        selectedTab.build(this);
    }

    protected abstract void buildHomeTab();

    protected void buildTeamSelectTab() {
        FlowLayout headerRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        headerRow.verticalAlignment(VerticalAlignment.CENTER);
        headerRow.child(UIComponents.label(AutomataTexts.getTeams()).shadow(true));
        headerRow.child(UIContainers.horizontalFlow(Sizing.expand(), Sizing.fixed(0)));
        headerRow.child(UIComponents.label(Text.literal(Integer.toString(handler.teams.size()))).shadow(true));
        body.child(headerRow);

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
                nameLabel.cursorStyle(CursorStyle.HAND);
                uuidLabel.cursorStyle(CursorStyle.HAND);
                teamRow.cursorStyle(CursorStyle.HAND);

                teamRow.mouseDown().subscribe((click, doubled) -> {
                    if (click.button() != 0)
                        return false;

                    AutomataMessages.CHANNEL.clientHandle().send(new BotTeamSelectMessage(Optional.of(team.UUID)));

                    return true;
                });
            }

            listContainer.child(teamRow);
        }
        body.child(listContainer);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.gap(BotDeviceScreenHandler.UI_SPACING);

        FlowLayout tabPanel = UIContainers.horizontalFlow(Sizing.fixed(BotDeviceScreenHandler.BODY_WIDTH),
                Sizing.fixed(AutomataClientTextures.BOT_DEVICE_ICONS.regionHeight()));
        tabPanel.surface(Surface.BLANK).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        tabPanel.gap(BotDeviceScreenHandler.UI_SPACING);
        for (Tab tab : Tab.values()) {
            tabPanel.child(tab.buildButton(this));
        }
        rootComponent.child(tabPanel);

        body = UIContainers.verticalFlow(Sizing.fixed(BotDeviceScreenHandler.BODY_WIDTH), Sizing.fixed(BotDeviceScreenHandler.BODY_HEIGHT));
        body.surface(Surface.PANEL).padding(Insets.of(BotDeviceScreenHandler.BODY_INSET));
        body.gap(BotDeviceScreenHandler.UI_SPACING);
        rootComponent.child(body);

        FlowLayout spacingPanel = UIContainers.horizontalFlow(Sizing.fixed(BotDeviceScreenHandler.BODY_WIDTH),
                Sizing.fixed(AutomataClientTextures.BOT_DEVICE_ICONS.regionHeight()));
        spacingPanel.surface(Surface.BLANK).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.child(spacingPanel);

        Tab.HOME.build(this);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }
}
