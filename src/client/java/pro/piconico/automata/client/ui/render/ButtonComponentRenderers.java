package pro.piconico.automata.client.ui.render;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import pro.piconico.automata.client.registry.AutomataClientColors;
import pro.piconico.automata.screen.ScreenHandlerUtils;

public class ButtonComponentRenderers {
    public static final float ENTITY_RENDER_SCALE = 8F;

    private static void drawBackground(OwoUIGraphics graphics, ButtonComponent button) {
        if (!button.active || !button.isHovered())
            return;

        int x2 = button.getX() + ScreenHandlerUtils.SLOT_SIZE;
        int y2 = button.getY() + ScreenHandlerUtils.SLOT_SIZE;
        graphics.fill(button.getX(), button.getY(), x2, y2, AutomataClientColors.HOVERED.argb());
    }

    public static ButtonComponent.Renderer item(ItemStack stack) {
        return (graphics, button, delta) -> {
            drawBackground(graphics, button);

            int x = button.getX() + (button.getWidth() - ScreenHandlerUtils.SLOT_SIZE) / 2;
            int y = button.getY() + (button.getHeight() - ScreenHandlerUtils.SLOT_SIZE) / 2;
            graphics.drawItem(stack, x, y);
        };
    }

    public static ButtonComponent.Renderer block(BlockState blockState) {
        return item(new ItemStack(blockState.getBlock()));
    }

    public static ButtonComponent.Renderer entity(Entity entity, float scale) {
        return (graphics, button, delta) -> {
            drawBackground(graphics, button);

            var renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            EntityRenderState entityRenderState = renderer.getAndUpdateRenderState(entity, scale);
            Vector3f translation = new Vector3f(0F, 0F, 0F);
            Quaternionf rotation = new Quaternionf().rotateLocalZ((float)Math.PI);
            graphics.addEntity(entityRenderState, scale, translation, rotation, null, button.getX(), button.getY(), button.getRight(), button.getBottom());
        };
    }

    public static ButtonComponent.Renderer entity(Entity entity) {
        return entity(entity, ENTITY_RENDER_SCALE);
    }
}
