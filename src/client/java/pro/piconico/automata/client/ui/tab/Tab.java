package pro.piconico.automata.client.ui.tab;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public interface Tab<HandlerT extends ScreenHandler> {
    public Text getName(HandlerT handler);

    public ButtonComponent.Renderer getButtonRenderer(HandlerT handler);

    public void build(HandlerT handler, FlowLayout parent);
}
