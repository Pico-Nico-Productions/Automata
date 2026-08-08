package pro.piconico.automata.util.math;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class ChunkUtils {
    public static final int CHUNK_SIZE = 16;

    public static BlockPos getStartPos(ChunkPos chunkPos, int minY) {
        return new BlockPos(chunkPos.getStartX(), minY, chunkPos.getStartZ());
    }

    public static BlockPos getEndPos(ChunkPos chunkPos, int maxY) {
        return new BlockPos(chunkPos.getEndX(), maxY, chunkPos.getEndZ());
    }

    public record ChunkBounds(ChunkPos minChunkPos, ChunkPos maxChunkPos, BlockPos minBlockPos, BlockPos maxBlockPos) {
        public static ChunkBounds of(ChunkPos center, int radius, int minY, int maxY) {
            if (radius < 0)
                throw new IllegalArgumentException("Radius must be >= 0");

            ChunkPos minChunkPos = new ChunkPos(center.x - radius, center.z - radius);
            ChunkPos maxChunkPos = new ChunkPos(center.x + radius, center.z + radius);
            BlockPos minBlockPos = getStartPos(minChunkPos, minY);
            BlockPos maxBlockPos = getEndPos(maxChunkPos, maxY);

            return new ChunkBounds(minChunkPos, maxChunkPos, minBlockPos, maxBlockPos);
        }

        public static ChunkBounds of(ChunkPos center, int radius, World world) {
            return of(center, radius, world.getBottomY(), world.getTopYInclusive());
        }

        public static ChunkBounds of(ChunkPos center, int radius) {
            return of(center, radius, World.MIN_Y, World.MAX_Y);
        }

        public boolean contains(ChunkPos chunkPos) {
            return chunkPos.x >= minChunkPos.x && chunkPos.z >= minChunkPos.z && chunkPos.x <= maxChunkPos.x && chunkPos.z <= maxChunkPos.z;
        }

        private boolean containsX(BlockPos blockPos) {
            return blockPos.getX() >= minBlockPos.getX() && blockPos.getX() <= maxBlockPos.getX();
        }

        private boolean containsY(BlockPos blockPos) {
            return blockPos.getY() >= minBlockPos.getY() && blockPos.getY() <= maxBlockPos.getY();
        }

        private boolean containsZ(BlockPos blockPos) {
            return blockPos.getZ() >= minBlockPos.getZ() && blockPos.getZ() <= maxBlockPos.getZ();
        }

        public boolean containsXZ(BlockPos blockPos) {
            return containsX(blockPos) && containsZ(blockPos);
        }

        public boolean contains(BlockPos blockPos) {
            return containsX(blockPos) && containsY(blockPos) && containsZ(blockPos);
        }
    }
}
