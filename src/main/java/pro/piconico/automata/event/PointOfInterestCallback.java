package pro.piconico.automata.event;

import java.util.Optional;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestType;

public interface PointOfInterestCallback {
    void onAction(ServerWorld serverWorld, BlockPos pos, Optional<BlockEntity> blockEntity, RegistryEntry<PointOfInterestType> pointOfInterestType);

    Event<PointOfInterestCallback> ADDED = EventFactory.createArrayBacked(PointOfInterestCallback.class, (listeners) -> (serverWorld, pos, blockEntity, pointOfInterestEntry) -> {
        for (PointOfInterestCallback event : listeners) {
            event.onAction(serverWorld, pos, blockEntity, pointOfInterestEntry);
        }
    });

    Event<PointOfInterestCallback> REMOVED = EventFactory.createArrayBacked(PointOfInterestCallback.class, (listeners) -> (serverWorld, pos, blockEntity, pointOfInterestEntry) -> {
        for (PointOfInterestCallback event : listeners) {
            event.onAction(serverWorld, pos, blockEntity, pointOfInterestEntry);
        }
    });
}
