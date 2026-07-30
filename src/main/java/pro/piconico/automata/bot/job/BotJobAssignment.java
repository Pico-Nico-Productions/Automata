package pro.piconico.automata.bot.job;

import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

public class BotJobAssignment<T extends BotJob> {
    public static <T extends BotJob> Codec<BotJobAssignment<T>> createCodec(MapCodec<T> jobCodec) {
        return RecordCodecBuilder.create(instance -> instance
                .group(
                    jobCodec.fieldOf("job").forGetter(BotJobAssignment::getJob),
                    Uuids.INT_STREAM_CODEC.optionalFieldOf("bot_uuid").forGetter(BotJobAssignment::getAssignedBot)
                )
                .apply(instance, BotJobAssignment::new)
        );
    }

    private Optional<UUID> assignedBot;

    public final T job;

    public BotJobAssignment(T job, Optional<UUID> assignedBot) {
        this.job = job;
        this.assignedBot = assignedBot;
    }

    public BotJobAssignment(T job) {
        this(job, Optional.empty());
    }

    public T getJob() {
        return job;
    }

    public Optional<UUID> getAssignedBot() {
        return assignedBot;
    }

    public boolean isAssigned() {
        return assignedBot.isPresent();
    }

    public void setAssignedBot(Optional<UUID> assignedBot) {
        this.assignedBot = assignedBot;
    }
}
