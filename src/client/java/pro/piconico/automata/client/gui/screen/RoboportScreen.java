package pro.piconico.automata.client.gui.screen;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import pro.piconico.automata.client.registry.AutomataClientTextures;
import pro.piconico.automata.client.registry.AutomataClientTextures.Texture;
import pro.piconico.automata.screen.RoboportScreenHandler;

public class RoboportScreen extends HandledScreen<RoboportScreenHandler> {
    public RoboportScreen(RoboportScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		this.drawMouseoverTooltip(context, mouseX, mouseY);
	}

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        Texture texture = AutomataClientTextures.ROBOPORT;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture.id(), x, y, 0, 0, texture.width(), texture.height(), texture.width(), texture.height());
    }
}
