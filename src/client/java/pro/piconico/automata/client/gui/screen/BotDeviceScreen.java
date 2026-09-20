package pro.piconico.automata.client.gui.screen;

import io.wispforest.owo.ui.base.BaseOwoContainerScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
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
import net.minecraft.text.Text;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.client.ui.tab.BotDeviceHomeTab;
import pro.piconico.automata.client.ui.tab.Tab;
import pro.piconico.automata.client.ui.tab.TabManager;
import pro.piconico.automata.client.ui.tab.TeamSelectTab;
import pro.piconico.automata.client.ui.tab.TeamSettingsTab;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.screen.ScreenConstants;

public abstract class BotDeviceScreen<HandlerT extends BotDeviceScreenHandler> extends BaseOwoContainerScreen<FlowLayout, HandlerT> {
    protected FlowLayout body;
    protected TabManager<BotDeviceScreenHandler, Tab<BotDeviceScreenHandler>> tabManager;

    public BotDeviceScreen(HandlerT handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    protected static void buildSlot(FlowLayout parent, int x, int y) {
        Sizing slotSize = Sizing.fixed(ScreenConstants.SLOT_DELTA);
        ParentUIComponent slot = UIContainers.horizontalFlow(slotSize, slotSize).surface(Surface.PANEL_INSET);
        parent.child(slot.positioning(Positioning.absolute(x - ScreenConstants.SLOT_SIZE / 2, y - ScreenConstants.SLOT_SIZE / 2)));
    }

    protected abstract BotDeviceHomeTab getHomeTab();

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.gap(BotDeviceScreenHandler.UI_SPACING);

        Sizing horizontalScreenSizing = Sizing.fixed(BotDeviceScreenHandler.BODY_WIDTH);
        Sizing verticalIconSizing = Sizing.fixed(AutomataClientTextures.BOT_DEVICE_ICONS.regionHeight());

        FlowLayout tabPanel = UIContainers.horizontalFlow(horizontalScreenSizing, verticalIconSizing);
        tabPanel.surface(Surface.BLANK).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        tabPanel.gap(BotDeviceScreenHandler.UI_SPACING);
        rootComponent.child(tabPanel);

        body = UIContainers.verticalFlow(horizontalScreenSizing, Sizing.fixed(BotDeviceScreenHandler.BODY_HEIGHT));
        body.surface(Surface.PANEL).padding(Insets.of(BotDeviceScreenHandler.BODY_INSET));
        body.gap(BotDeviceScreenHandler.UI_SPACING);
        rootComponent.child(body);

        FlowLayout spacingPanel = UIContainers.horizontalFlow(horizontalScreenSizing, verticalIconSizing);
        rootComponent.child(spacingPanel);

        tabManager = new TabManager<>(handler, body, getHomeTab(), TeamSelectTab.INSTANCE, TeamSettingsTab.INSTANCE);
        tabManager.buildButtons(tabPanel);
    }

    public void rebuildTab() {
        tabManager.rebuildTab();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }
}
