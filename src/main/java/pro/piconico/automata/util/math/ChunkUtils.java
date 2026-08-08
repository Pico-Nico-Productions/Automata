package pro.piconico.automata.util.math;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
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
        public static ChunkBounds of(ChunkPos center, int chunkRadius, int minY, int maxY) {
            if (chunkRadius < 0)
                throw new IllegalArgumentException("Chunk radius must be >= 0");

            ChunkPos minChunkPos = new ChunkPos(center.x - chunkRadius, center.z - chunkRadius);
            ChunkPos maxChunkPos = new ChunkPos(center.x + chunkRadius, center.z + chunkRadius);
            BlockPos minBlockPos = getStartPos(minChunkPos, minY);
            BlockPos maxBlockPos = getEndPos(maxChunkPos, maxY);

            return new ChunkBounds(minChunkPos, maxChunkPos, minBlockPos, maxBlockPos);
        }

        public static ChunkBounds of(ChunkPos center, int chunkRadius, HeightLimitView heightLimitView) {
            return of(center, chunkRadius, heightLimitView.getBottomY(), heightLimitView.getTopYInclusive());
        }

        public static ChunkBounds of(ChunkPos center, int chunkRadius) {
            return of(center, chunkRadius, World.MIN_Y, World.MAX_Y);
        }

        public Box toBox() {
            return new Box(
                minBlockPos.getX(),
                minBlockPos.getY(),
                minBlockPos.getZ(),
                maxBlockPos.getX() + 1.0,
                maxBlockPos.getY() + 1.0,
                maxBlockPos.getZ() + 1.0
            );
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
