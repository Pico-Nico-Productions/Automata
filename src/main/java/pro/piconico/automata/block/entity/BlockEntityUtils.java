package pro.piconico.automata.block.entity;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;

public class BlockEntityUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final NbtCompound createNbtWithId(BlockEntity blockEntity, RegistryWrapper.WrapperLookup registries) {
        ErrorReporter.Logging logging = new ErrorReporter.Logging(blockEntity.getReporterContext(), LOGGER);
        NbtCompound nbt;

        try {
            NbtWriteView nbtWriteView = NbtWriteView.create(logging, registries);
            blockEntity.writeDataWithId(nbtWriteView);
            nbt = nbtWriteView.getNbt();
        }
        catch (Throwable mainThrowable) {
            try {
                logging.close();
            }
            catch (Throwable subThrowable) {
                mainThrowable.addSuppressed(subThrowable);
            }

            throw mainThrowable;
        }

        logging.close();
        return nbt;
    }
}
