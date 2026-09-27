package pro.piconico.automata.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pro.piconico.automata.event.PointOfInterestCallback;

@Mixin(WorldChunk.class)
public abstract class PointOfInterestEventMixin {
    @Unique
    private static final class PointOfInterestChangeContext {
        private final BlockPos pos;
        private final Optional<RegistryEntry<PointOfInterestType>> oldType;
        private final Optional<RegistryEntry<PointOfInterestType>> newType;
        private final BlockEntity oldBlockEntity;

        private boolean removedInvoked;

        private PointOfInterestChangeContext(BlockPos pos, Optional<RegistryEntry<PointOfInterestType>> oldType, Optional<RegistryEntry<PointOfInterestType>> newType,
                BlockEntity oldBlockEntity) {
            this.pos = pos;
            this.oldType = oldType;
            this.newType = newType;
            this.oldBlockEntity = oldBlockEntity;
        }

        public BlockPos pos() {
            return pos;
        }

        public boolean removedInvoked() {
            return removedInvoked;
        }

        public void markRemovedInvoked() {
            removedInvoked = true;
        }
    }

    @Unique
    private final ThreadLocal<Deque<PointOfInterestChangeContext>> pointOfInterestChanges = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void capturePointOfInterestChange(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<BlockState> callbackInfo) {
        WorldChunk chunk = (WorldChunk)(Object)this;
        Optional<RegistryEntry<PointOfInterestType>> oldType = PointOfInterestTypes.getTypeForState(chunk.getBlockState(pos));
        Optional<RegistryEntry<PointOfInterestType>> newType = PointOfInterestTypes.getTypeForState(newState);

        if (Objects.equals(oldType, newType))
            return;

        BlockEntity oldBlockEntity = chunk.getBlockEntity(pos);
        pointOfInterestChanges.get().push(new PointOfInterestChangeContext(pos.toImmutable(), oldType, newType, oldBlockEntity));
    }

    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void finishPointOfInterestChange(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<BlockState> callbackInfo) {
        WorldChunk chunk = (WorldChunk)(Object)this;

        if (!(chunk.getWorld() instanceof ServerWorld serverWorld))
            return;

        Deque<PointOfInterestChangeContext> changes = pointOfInterestChanges.get();

        if (changes.isEmpty())
            return;

        PointOfInterestChangeContext change = changes.pop();
        try {
            if (callbackInfo.getReturnValue() == null)
                return;

            if (!change.removedInvoked() && change.oldType.isPresent() && !Objects.equals(change.oldType, change.newType)) {
                PointOfInterestCallback.REMOVED.invoker().onAction(serverWorld, change.pos(), Optional.ofNullable(change.oldBlockEntity),
                        change.oldType.get());

                change.markRemovedInvoked();
            }

            if (change.newType.isPresent()) {
                BlockEntity newBlockEntity = chunk.getBlockEntity(change.pos());

                PointOfInterestCallback.ADDED.invoker().onAction(serverWorld, change.pos(), Optional.ofNullable(newBlockEntity), change.newType.get());
            }
        }
        finally {
            if (changes.isEmpty()) {
                pointOfInterestChanges.remove();
            }
        }
    }

    @Inject(method = "removeBlockEntity", at = @At("HEAD"))
    private void invokePointOfInterestRemoved(BlockPos pos, CallbackInfo callbackInfo) {
        WorldChunk chunk = (WorldChunk)(Object)this;

        if (!(chunk.getWorld() instanceof ServerWorld serverWorld))
            return;

        Deque<PointOfInterestChangeContext> changes = pointOfInterestChanges.get();

        if (changes.isEmpty())
            return;

        PointOfInterestChangeContext change = changes.peek();

        if (!change.pos().equals(pos) || change.oldType.isEmpty() || Objects.equals(change.oldType, change.newType))
            return;

        PointOfInterestCallback.REMOVED.invoker().onAction(serverWorld, change.pos(), Optional.ofNullable(change.oldBlockEntity), change.oldType.get());

        change.markRemovedInvoked();
    }
}