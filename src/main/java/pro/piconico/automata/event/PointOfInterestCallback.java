package pro.piconico.automata.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestType;

public interface PointOfInterestCallback {
    void onAction(ServerWorld serverWorld, BlockPos pos, RegistryEntry<PointOfInterestType> pointOfInterestType);

    Event<PointOfInterestCallback> ADDED = EventFactory.createArrayBacked(PointOfInterestCallback.class, (listeners) -> (serverWorld, pos, pointOfInterestEntry) -> {
        for (PointOfInterestCallback event : listeners) {
            event.onAction(serverWorld, pos, pointOfInterestEntry);
        }
    });

    Event<PointOfInterestCallback> REMOVED = EventFactory.createArrayBacked(PointOfInterestCallback.class, (listeners) -> (serverWorld, pos, pointOfInterestEntry) -> {
        for (PointOfInterestCallback event : listeners) {
            event.onAction(serverWorld, pos, pointOfInterestEntry);
        }
    });
}
