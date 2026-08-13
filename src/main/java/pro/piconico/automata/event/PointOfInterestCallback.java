package pro.piconico.automata.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestType;

public interface PointOfInterestCallback {
    void onAction(BlockPos pos, RegistryEntry<PointOfInterestType> pointOfInterestType, ServerWorld serverWorld);

    Event<PointOfInterestCallback> ADDED = EventFactory.createArrayBacked(PointOfInterestCallback.class, (listeners) -> (pos, pointOfInterestEntry, serverWorld) -> {
        for (PointOfInterestCallback event : listeners) {
            event.onAction(pos, pointOfInterestEntry, serverWorld);
        }
    });

    Event<PointOfInterestCallback> REMOVED = EventFactory.createArrayBacked(PointOfInterestCallback.class, (listeners) -> (pos, pointOfInterestEntry, serverWorld) -> {
        for (PointOfInterestCallback event : listeners) {
            event.onAction(pos, pointOfInterestEntry, serverWorld);
        }
    });
}
