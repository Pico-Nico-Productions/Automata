package pro.piconico.automata.client.gui.screen;

import java.util.Optional;
import java.util.UUID;
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
import net.minecraft.inventory.Inventory;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import pro.piconico.automata.bot.device.BotDevice;
import pro.piconico.automata.bot.device.BotDevice.ChangeBotTeam;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.client.ui.tab.BotDeviceHomeTab;
import pro.piconico.automata.client.ui.tab.Tab;
import pro.piconico.automata.client.ui.tab.TabManager;
import pro.piconico.automata.client.ui.tab.TeamCreateTab;
import pro.piconico.automata.client.ui.tab.TeamSelectTab;
import pro.piconico.automata.client.ui.tab.TeamSettingsTab;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.screen.ScreenHandlerUtils;

public abstract class BotDeviceScreen<HandlerT extends BotDeviceScreenHandler> extends BaseOwoContainerScreen<FlowLayout, HandlerT> {
    protected FlowLayout body;
    protected TabManager<BotDeviceScreenHandler, Tab<BotDeviceScreenHandler>> tabManager;

    public BotDeviceScreen(HandlerT handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        BotDevice.TEAM_CHANGED_CALLBACKS.add(this::onTeamChanged);

        backgroundWidth = handler.getBodyWidth();
        backgroundHeight = handler.getBodyHeight();
    }

    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    public void removed() {
        BotDevice.TEAM_CHANGED_CALLBACKS.remove((ChangeBotTeam)this::onTeamChanged);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }

    protected static void buildSlot(FlowLayout parent, int x, int y) {
        Sizing slotSize = Sizing.fixed(ScreenHandlerUtils.SLOT_DELTA);
        ParentUIComponent slot = UIContainers.horizontalFlow(slotSize, slotSize).surface(Surface.PANEL_INSET);
        int absoluteX = x - parent.padding().get().left() - 1;
        int absoluteY = y - parent.padding().get().top() - 1;
        parent.child(slot.positioning(Positioning.absolute(absoluteX, absoluteY)));
    }

    protected static void buildDeviceAndPlayerSlots(BotDeviceScreenHandler handler, FlowLayout parent) {
        int deviceInventorySize = handler.device instanceof Inventory inventory ? inventory.size() : 0;
        int deviceRowCount = ScreenHandlerUtils.getRowCount(deviceInventorySize);
        int inventoryX = handler.getInventoryX();

        for (int yIndex = 0; yIndex < deviceRowCount; yIndex++) {
            for (int xIndex = 0; xIndex < 9; xIndex++) {
                int index = 9 * yIndex + xIndex;

                if (index >= deviceInventorySize)
                    break;

                int x = inventoryX + xIndex * ScreenHandlerUtils.SLOT_DELTA;
                int y = BotDeviceScreenHandler.DEVICE_INVENTORY_Y + yIndex * ScreenHandlerUtils.SLOT_DELTA;
                buildSlot(parent, x, y);
            }
        }

        int playerInventoryY = BotDeviceScreenHandler.DEVICE_INVENTORY_Y + deviceRowCount * ScreenHandlerUtils.SLOT_DELTA
                + BotDeviceScreenHandler.DEVICE_PLAYER_GAP;
        for (int yIndex = 0; yIndex < 3; yIndex++) {
            for (int xIndex = 0; xIndex < 9; xIndex++) {
                int x = inventoryX + xIndex * ScreenHandlerUtils.SLOT_DELTA;
                int y = playerInventoryY + yIndex * ScreenHandlerUtils.SLOT_DELTA;
                BotDeviceScreen.buildSlot(parent, x, y);
            }
        }

        int hotbarY = playerInventoryY + 2 * ScreenHandlerUtils.SLOT_DELTA + ScreenHandlerUtils.BAR_DELTA;
        for (int indexX = 0; indexX < 9; indexX++) {
            int x = inventoryX + indexX * ScreenHandlerUtils.SLOT_DELTA;
            BotDeviceScreen.buildSlot(parent, x, hotbarY);
        }
    }

    protected abstract BotDeviceHomeTab getHomeTab();

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.gap(BotDeviceScreenHandler.GAP);

        Sizing horizontalScreenSizing = Sizing.fixed(handler.getBodyWidth());
        Sizing verticalIconSizing = Sizing.fixed(AutomataClientTextures.BOT_DEVICE_ICONS.regionHeight());

        FlowLayout tabPanel = UIContainers.horizontalFlow(horizontalScreenSizing, verticalIconSizing);
        tabPanel.surface(Surface.BLANK).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        tabPanel.gap(BotDeviceScreenHandler.GAP);
        rootComponent.child(tabPanel);

        body = UIContainers.verticalFlow(horizontalScreenSizing, Sizing.fixed(handler.getBodyHeight()));
        body.surface(Surface.DARK_PANEL).padding(Insets.of(BotDeviceScreenHandler.INSET));
        body.gap(BotDeviceScreenHandler.GAP);
        rootComponent.child(body);

        FlowLayout spacingPanel = UIContainers.horizontalFlow(horizontalScreenSizing, verticalIconSizing);
        rootComponent.child(spacingPanel);

        tabManager = new TabManager<>(handler, body, getHomeTab(), TeamSelectTab.INSTANCE, TeamSettingsTab.INSTANCE, TeamCreateTab.INSTANCE);
        tabManager.buildButtons(tabPanel);
    }

    public void rebuildTab() {
        tabManager.rebuildTab();
    }

    private void onTeamChanged(World world, BotDevice<?> device, Optional<UUID> oldTeamUuid) {
        if (!handler.device.equals(device))
            return;

        rebuildTab();
    }
}
