package pro.piconico.automata.util.math;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;

public class BlockPosUtils {
    public static Optional<BlockPos> min(Collection<BlockPos> positions) {
        if (positions.isEmpty())
            return Optional.empty();

        int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, z = Integer.MAX_VALUE;
        for (BlockPos blockPos : positions) {
            x = Integer.min(x, blockPos.getX());
            y = Integer.min(y, blockPos.getY());
            z = Integer.min(z, blockPos.getZ());
        }

        return Optional.of(new BlockPos(x, y, z));
    }

    public static Optional<BlockPos> max(Collection<BlockPos> positions) {
        if (positions.isEmpty())
            return Optional.empty();

        int x = Integer.MIN_VALUE, y = Integer.MIN_VALUE, z = Integer.MIN_VALUE;
        for (BlockPos blockPos : positions) {
            x = Integer.max(x, blockPos.getX());
            y = Integer.max(y, blockPos.getY());
            z = Integer.max(z, blockPos.getZ());
        }

        return Optional.of(new BlockPos(x, y, z));
    }
}
