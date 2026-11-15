package pro.piconico.automata.client.render.overlay;

import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import org.joml.Quaternionf;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import pro.piconico.automata.client.registry.AutomataClientColors;
import pro.piconico.automata.client.render.RenderUtils;
import pro.piconico.automata.component.BlueprintComponent;
import pro.piconico.automata.component.BlueprintComponent.BlueprintBlockState;
import pro.piconico.automata.entity.LivingEntityUtils;
import pro.piconico.automata.util.math.BlockPosUtils;

public class BlueprintRenderer {
    private static void renderBlueprint(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        SequencedMap<Hand, ItemStack> stacks = LivingEntityUtils.getHeldStacks(client.player, BlueprintComponent::exists);

        if (stacks.isEmpty())
            return;

        Map<BlockPos, BlueprintBlockState> blockStates = BlueprintComponent.get(stacks.firstEntry().getValue());
        BlockPos anchor = client.crosshairTarget instanceof BlockHitResult blockHit ? blockHit.getBlockPos().offset(blockHit.getSide())
                : BlockPos.ofFloored(client.player.getEyePos().add(client.player.getRotationVector().multiply(client.player.getBlockInteractionRange())));
        Direction facing = client.player.getHorizontalFacing();
        float yaw = (float)Math.atan2(facing.getOffsetX(), facing.getOffsetZ());
        for (BlueprintBlockState blockState : blockStates.values()) {
            BlockPos worldPos = anchor.add(BlockPosUtils.rotateRelativePos(blockState.relativePos(), facing));
            Quaternionf rotation = RotationAxis.POSITIVE_Y.rotation(yaw);
            Optional<BlockEntity> blockEntity = blockState.entityData()
                    .map(data -> BlockEntity.createFromNbt(worldPos, blockState.state(), data, client.world.getRegistryManager()));
            RenderUtils.drawBlock(context, worldPos, rotation, blockState.state(), blockEntity, AutomataClientColors.BLUEPRINT);
        }
    }

    public static void initialize() {
        WorldRenderEvents.AFTER_ENTITIES.register(BlueprintRenderer::renderBlueprint);
    }
}
