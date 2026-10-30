package pro.piconico.automata.client.ui;

import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.MinecraftClient;

public class UIComponentUtils {
    public static boolean isHovered(UIComponent component) {
        MinecraftClient client = MinecraftClient.getInstance();
        int scale = client.getWindow().getScaleFactor();
        int mouseX = (int)(client.mouse.getX() / scale), mouseY = (int)(client.mouse.getY() / scale);
        return mouseX >= component.x() && mouseX < component.x() + component.width() && mouseY >= component.y() && mouseY < component.y() + component.height();
    }
}
