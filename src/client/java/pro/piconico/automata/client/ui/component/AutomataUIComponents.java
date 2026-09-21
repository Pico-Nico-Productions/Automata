package pro.piconico.automata.client.ui.component;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;

public class AutomataUIComponents {
    public static FlowLayout centerHeader(Text text) {
        FlowLayout headerRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        headerRow.horizontalAlignment(HorizontalAlignment.CENTER);
        headerRow.child(UIComponents.label(text).shadow(true));

        return headerRow;
    }
}
