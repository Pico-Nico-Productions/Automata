package pro.piconico.automata.client.ui.tab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import pro.piconico.automata.Automata;
import pro.piconico.automata.screen.ScreenHandlerUtils;
import pro.piconico.automata.screen.slot.DisableableSlot;

public class TabManager<HandlerT extends ScreenHandler, TabT extends Tab<HandlerT>> {
    private final HandlerT handler;
    private final FlowLayout parent;
    private final List<TabT> tabList;
    public final Set<TabT> tabSet;
    private TabT selectedTab;

    public TabManager(HandlerT handler, FlowLayout parent, List<TabT> tabs) {
        if (tabs.size() == 0)
            throw new IllegalArgumentException(TabManager.class.getSimpleName() + " needs at least one tab");

        this.handler = handler;
        this.parent = parent;
        tabList = new ArrayList<>(tabs);
        tabSet = new HashSet<>(tabs);

        selectTab(tabs.getFirst());
    }

    public void rebuildTab() {
        parent.clearChildren();
        for (Slot slot : handler.slots) {
            DisableableSlot.setEnabled(slot, false);
        }
        selectedTab.build(handler, parent);
    }

    public void selectTab(TabT tab) {
        if (!tabSet.contains(tab)) {
            Automata.logError("This " + TabManager.class.getSimpleName() + " doesn't have " + tab.getName(handler).getString(), IllegalArgumentException::new);
            return;
        }

        if (tab == selectedTab)
            return;

        selectedTab = tab;
        rebuildTab();
    }

    public void buildButtons(FlowLayout buttonParent) {
        for (TabT tab : tabList) {
            ButtonComponent button = UIComponents.button(Text.empty(), ignored -> selectTab(tab));
            button.sizing(Sizing.fixed(ScreenHandlerUtils.SLOT_SIZE));
            button.renderer(tab.getButtonRenderer(handler));
            button.tooltip(tab.getName(handler));
            buttonParent.child(button);
        }
    }
}
