package pro.piconico.automata.mixin;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.serialization.Codec;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ChunkErrorHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestSet.Serialized;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import pro.piconico.automata.event.PointOfInterestCallback;

@Mixin(PointOfInterestStorage.class)
public abstract class PointOfInterestEventMixin extends SerializingRegionBasedStorage<PointOfInterestSet, PointOfInterestSet.Serialized> {
    @Unique
    private final ThreadLocal<Optional<RegistryEntry<PointOfInterestType>>> cachedPointOfInterestType = new ThreadLocal<>();

    @Shadow
    protected abstract Optional<RegistryEntry<PointOfInterestType>> getType(BlockPos pos);

    public PointOfInterestEventMixin(VersionedChunkStorage storageAccess, Codec<Serialized> codec, Function<PointOfInterestSet, Serialized> serializer,
            BiFunction<Serialized, Runnable, PointOfInterestSet> deserializer, Function<Runnable, PointOfInterestSet> factory,
            DynamicRegistryManager registryManager, ChunkErrorHandler errorHandler, HeightLimitView world) {
        super(storageAccess, codec, serializer, deserializer, factory, registryManager, errorHandler, world);
    }

    @Inject(method = "add", at = @At("RETURN"))
    private void invokeAddedEvent(BlockPos pos, RegistryEntry<PointOfInterestType> type, CallbackInfoReturnable<PointOfInterest> callbackInfo) {
        PointOfInterest originalResult = callbackInfo.getReturnValue();
        if (originalResult == null)
            return;

        PointOfInterestCallback.ADDED.invoker().onAction(pos, type, (ServerWorld)this.world);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void cacheRemoveType(BlockPos pos, CallbackInfo callbackInfo) {
        cachedPointOfInterestType.set(this.getType(pos));
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void invokeRemovedEvent(BlockPos pos, CallbackInfo callbackInfo) {
        Optional<RegistryEntry<PointOfInterestType>> pointOfInterestType = cachedPointOfInterestType.get();
        cachedPointOfInterestType.remove();

        if (pointOfInterestType.isEmpty())
            return;

        PointOfInterestCallback.REMOVED.invoker().onAction(pos, pointOfInterestType.get(), (ServerWorld)this.world);
    }
}
