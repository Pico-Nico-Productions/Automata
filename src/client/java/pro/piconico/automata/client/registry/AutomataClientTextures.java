package pro.piconico.automata.client.registry;

import java.util.function.Consumer;
import org.spongepowered.asm.mixin.injection.invoke.arg.ArgumentIndexOutOfBoundsException;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pro.piconico.automata.client.design.AutomataColors;
import pro.piconico.automata.registry.AutomataRegistry;

public class AutomataClientTextures {
    public record Texture(Identifier id, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        public Texture(Identifier id, int width, int height) {
            this(id, 0, 0, width, height, width, height);
        }

        public TextureComponent toComponent() {
            return UIComponents.texture(id, u, v, width, height, textureWidth, textureHeight);
        }

        public ButtonComponent.Renderer getButtonRenderer() {
            return (graphics, button, delta) -> {
                if (button.active && button.isHovered()) {
                    graphics.fill(button.getX(), button.getY(), button.getRight(), button.getBottom(), AutomataColors.HOVERED);
                }

                int x = button.getX() + (button.getWidth() - width) / 2;
                int y = button.getY() + (button.getHeight() - height) / 2;
                int color = button.active() ? AutomataColors.ACTIVE : AutomataColors.INACTIVE;
                graphics.drawTexture(RenderPipelines.GUI_TEXTURED, id, x, y, u, v, width, height, textureWidth, textureHeight, color);
            };
        }

        public ButtonComponent toButtonComponent(Text text, Consumer<ButtonComponent> onPress) {
            ButtonComponent buttonComponent = UIComponents.button(text, onPress);
            buttonComponent.sizing(Sizing.fixed(width), Sizing.fixed(height));
            buttonComponent.renderer(getButtonRenderer());

            return buttonComponent;
        }
    }

    public record TextureSheet(Identifier id, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        public int regionCount() {
            return (textureWidth / regionHeight) * (textureHeight / regionHeight);
        }

        public Texture getTexture(int regionIndex) {
            if (regionIndex >= regionCount())
                throw new ArgumentIndexOutOfBoundsException(regionIndex);

            int columns = textureWidth / regionWidth;
            int column = regionIndex % columns;
            int row = regionIndex / columns;

            int u = column * regionWidth;
            int v = row * regionHeight;

            return new Texture(id, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
        }
    }

    public static final Texture CONSTRUCTION_BOT = new Texture(AutomataClientRegistry.entityTextureId(AutomataRegistry.CONSTRUCTION_BOT), 64, 64);

    public static final TextureSheet BOT_DEVICE_ICONS = new TextureSheet(AutomataClientRegistry.screenGuiTextureId(AutomataClientRegistry.BOT_DEVICE_ICONS), 16,
            16, 64, 64);
    public static final Texture BOT_DEVICE_HOME_ICON = BOT_DEVICE_ICONS.getTexture(0);
    public static final Texture BOT_DEVICE_TEAM_SELECT_ICON = BOT_DEVICE_ICONS.getTexture(1);
    public static final Texture BOT_DEVICE_TEAM_SETTINGS_ICON = BOT_DEVICE_ICONS.getTexture(2);
    public static final Texture BOT_DEVICE_TEAM_ADD_ICON = BOT_DEVICE_ICONS.getTexture(3);
}
