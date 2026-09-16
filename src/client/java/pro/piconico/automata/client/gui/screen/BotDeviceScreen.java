package pro.piconico.automata.client.gui.screen;

import java.util.function.Consumer;
import io.wispforest.owo.ui.base.BaseOwoContainerScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.registry.AutomataTexts;

public abstract class BotDeviceScreen<HandlerT extends ScreenHandler> extends BaseOwoContainerScreen<FlowLayout, HandlerT> {
    protected static final int SPACING = 4;

    protected enum Tab {
        HOME(deviceScreen -> deviceScreen.buildHomeTab(), AutomataTexts.BOT_DEVICE_HOME), //
        TEAM_SELECT(deviceScreen -> deviceScreen.buildTeamSelectTab(), AutomataTexts.BOT_DEVICE_TEAM_SELECT);

        private final Consumer<BotDeviceScreen<?>> build;
        private final String translationKey;

        private Tab(Consumer<BotDeviceScreen<?>> build, String translationKey) {
            this.translationKey = translationKey;
            this.build = build;
        }

        private ButtonComponent buildButton(BotDeviceScreen<?> deviceScreen) {
            Consumer<ButtonComponent> onPress = button -> {
                deviceScreen.body.clearChildren();
                build.accept(deviceScreen);
            };
            ButtonComponent button = AutomataClientTextures.BOT_DEVICE_ICONS.toButtonComponent(ordinal(), Text.empty(), onPress);
            button.tooltip(Text.translatable(translationKey));

            return button;
        }
    }

    protected FlowLayout body;

    public BotDeviceScreen(HandlerT handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    protected abstract void buildHomeTab();

    protected void buildTeamSelectTab() {
        body.child(UIComponents.label(Text.translatable(AutomataTexts.TEAMS)).shadow(true));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.gap(SPACING);

        FlowLayout tabPanel = UIContainers.horizontalFlow(Sizing.fixed(176), Sizing.fixed(AutomataClientTextures.BOT_DEVICE_ICONS.regionHeight()));
        tabPanel.surface(Surface.BLANK).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        tabPanel.gap(SPACING);
        for (Tab tab : Tab.values()) {
            tabPanel.child(tab.buildButton(this));
        }
        rootComponent.child(tabPanel);

        body = UIContainers.verticalFlow(Sizing.fixed(176), Sizing.fixed(166));
        body.surface(Surface.PANEL).padding(Insets.of(SPACING));
        body.gap(SPACING);
        rootComponent.child(body);

        buildHomeTab();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }
}
