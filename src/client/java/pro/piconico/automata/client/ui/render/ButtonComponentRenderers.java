package pro.piconico.automata.client.ui.render;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import pro.piconico.automata.client.design.AutomataColors;
import pro.piconico.automata.screen.ScreenConstants;

public class ButtonComponentRenderers {
    private static void drawBackground(OwoUIGraphics graphics, ButtonComponent button) {
        if (!button.active || !button.isHovered())
            return;

        int x2 = button.getX() + ScreenConstants.SLOT_SIZE;
        int y2 = button.getY() + ScreenConstants.SLOT_SIZE;
        graphics.fill(button.getX(), button.getY(), x2, y2, AutomataColors.HOVERED);
    }

    public static ButtonComponent.Renderer item(ItemStack stack) {
        return (graphics, button, delta) -> {
            drawBackground(graphics, button);

            int x = button.getX() + (button.getWidth() - ScreenConstants.SLOT_SIZE) / 2;
            int y = button.getY() + (button.getHeight() - ScreenConstants.SLOT_SIZE) / 2;
            graphics.drawItem(stack, x, y);
        };
    }

    public static ButtonComponent.Renderer block(BlockState blockState) {
        return item(new ItemStack(blockState.getBlock()));
    }
}
