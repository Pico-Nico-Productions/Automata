package pro.piconico.automata.client.registry;

import java.util.function.Consumer;
import org.joml.Vector2i;
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
    public record Texture(Identifier id, int regionWidth, int regionHeight, int width, int height) {
        public Texture(Identifier id, int width, int height) {
            this(id, width, height, width, height);
        }

        public int regionCount() {
            return (width / regionHeight) * (height / regionHeight);
        }

        private Vector2i getRegionUV(int regionIndex) {
            if (regionIndex >= regionCount())
                throw new ArgumentIndexOutOfBoundsException(regionIndex);

            int columns = width / regionWidth;
            int column = regionIndex % columns;
            int row = regionIndex / columns;

            int u = column * regionWidth;
            int v = row * regionHeight;

            return new Vector2i(u, v);
        }

        public TextureComponent toComponent(int regionIndex) {
            Vector2i uv = getRegionUV(regionIndex);
            return UIComponents.texture(id, uv.x, uv.y, regionWidth, regionHeight, width, height);
        }

        public TextureComponent toComponent() {
            return toComponent(0);
        }

        public ButtonComponent toButtonComponent(int regionIndex, Text text, Consumer<ButtonComponent> onPress) {
            ButtonComponent buttonComponent = UIComponents.button(text, onPress);
            buttonComponent.sizing(Sizing.fixed(regionWidth), Sizing.fixed(regionHeight));
            Vector2i regionUV = getRegionUV(regionIndex);
            buttonComponent.renderer((graphics, button, delta) -> {
                if (button.active && button.isHovered()) {
                    graphics.fill(button.getX(), button.getY(), button.getX() + regionWidth, button.getY() + regionHeight, AutomataColors.HOVERED);
                }

                int argbColor = button.active() ? AutomataColors.ACTIVE : AutomataColors.INACTIVE;
                graphics.drawTexture(RenderPipelines.GUI_TEXTURED, AutomataClientTextures.BOT_DEVICE_ICONS.id, button.getX(), button.getY(), regionUV.x,
                        regionUV.y, regionWidth, regionHeight, width, height, argbColor);
            });

            return buttonComponent;
        }

        public ButtonComponent toButtonComponent(Text text, Consumer<ButtonComponent> onPress) {
            return toButtonComponent(0, text, onPress);
        }
    }

    public static final Texture CONSTRUCTION_BOT = new Texture(AutomataClientRegistry.entityTextureId(AutomataRegistry.CONSTRUCTION_BOT), 64, 64);

    public static final Texture BOT_DEVICE_ICONS = new Texture(AutomataClientRegistry.screenGuiTextureId(AutomataClientRegistry.BOT_DEVICES), 16, 16, 64, 64);
    public static final Texture ROBOPORT = new Texture(AutomataClientRegistry.screenGuiTextureId(AutomataRegistry.ROBOPORT), 176, 166);
}
