package pro.piconico.automata.client.registry;

import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import pro.piconico.automata.registry.AutomataRegistry;

public class AutomataClientSprites {
    public static final SpriteIdentifier LOGISTIC_CHEST = TexturedRenderLayers.CHEST_SPRITE_MAPPER.map(AutomataRegistry.id(AutomataRegistry.LOGISTIC_CHEST));
}
