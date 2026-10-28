package pro.piconico.automata.client.render;

import java.util.SequencedMap;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import pro.piconico.automata.client.registry.AutomataClientColors;
import pro.piconico.automata.component.SelectionComponent;
import pro.piconico.automata.entity.LivingEntityUtils;

public class SelectionRenderer {
    private static void renderSelection(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        SequencedMap<Hand, ItemStack> stacks = LivingEntityUtils.getHeldStacks(client.player, stack -> SelectionComponent.get(stack).isPresent());

        if (stacks.isEmpty())
            return;

        SelectionComponent selection = SelectionComponent.get(stacks.firstEntry().getValue()).get();

        if (!client.world.getRegistryKey().getValue().equals(selection.worldId()))
            return;

        selection.getSelectionBox().ifPresent(box -> RenderUtils.drawBoxOutline(context, box, AutomataClientColors.SELECTION_BOUNDS));

        if (selection.selection1().equals(selection.selection2()))
            return;

        selection.selection1().ifPresent(selection1 -> RenderUtils.drawBoxOutline(context, new Box(selection1), AutomataClientColors.SELECTION1));
        selection.selection2().ifPresent(selection2 -> RenderUtils.drawBoxOutline(context, new Box(selection2), AutomataClientColors.SELECTION2));
    }

    public static void initialize() {
        WorldRenderEvents.END_MAIN.register(SelectionRenderer::renderSelection);
    }
}
