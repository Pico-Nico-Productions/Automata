package pro.piconico.automata.client.ui.component;

import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.event.MouseDown;
import net.minecraft.text.Text;
import pro.piconico.automata.client.registry.AutomataClientColors;
import pro.piconico.automata.client.ui.UIComponentUtils;

public class AutomataUIComponents {
    public static final int PADDING = 2;

    public static FlowLayout centerHeader(Text text) {
        FlowLayout headerRow = UIContainers.horizontalFlow(Sizing.fill(), Sizing.content());
        headerRow.horizontalAlignment(HorizontalAlignment.CENTER);
        headerRow.child(UIComponents.label(text).shadow(true));

        return headerRow;
    }

    public static FlowLayout centerBody(Text text) {
        FlowLayout bodyRow = UIContainers.horizontalFlow(Sizing.fill(), Sizing.content());
        bodyRow.horizontalAlignment(HorizontalAlignment.CENTER);
        bodyRow.child(UIComponents.label(text));

        return bodyRow;
    }

    public static Surface hoverSurface(Color color, Color outlineColor, Color hoverColor) {
        return (graphics, parent) -> {
            int left = parent.x(), right = left + parent.width(), top = parent.y(), bottom = top + parent.height();
            graphics.fill(left, top, right, bottom, (UIComponentUtils.isHovered(parent) ? hoverColor : color).argb());
            graphics.drawRectOutline(left, top, parent.width(), parent.height(), outlineColor.argb());
        };
    }

    public static FlowLayout clickableRow(Color color, Color outlineColor, Color hoverColor, MouseDown onMouseDown, BaseUIComponent... children) {
        FlowLayout layout = UIContainers.horizontalFlow(Sizing.fill(), Sizing.content());
        layout.padding(Insets.of(PADDING));
        layout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        layout.surface(hoverSurface(color, outlineColor, hoverColor));
        layout.cursorStyle(CursorStyle.HAND);
        layout.mouseDown().subscribe(onMouseDown);

        for (BaseUIComponent component : children) {
            component.cursorStyle(CursorStyle.HAND);
            layout.child(component);
        }

        return layout;
    }

    public static FlowLayout clickableRow(Color color, Color outlineColor, MouseDown onMouseDown, BaseUIComponent... children) {
        return clickableRow(color, outlineColor, AutomataClientColors.BUTTON_HOVERED, onMouseDown, children);
    }

    public static FlowLayout clickableRow(Color color, MouseDown onMouseDown, BaseUIComponent... children) {
        return clickableRow(color, AutomataClientColors.BUTTON_OUTLINE, onMouseDown, children);
    }

    public static FlowLayout clickableRow(MouseDown onMouseDown, BaseUIComponent... children) {
        return clickableRow(AutomataClientColors.BUTTON_ACTIVE, onMouseDown, children);
    }
}
