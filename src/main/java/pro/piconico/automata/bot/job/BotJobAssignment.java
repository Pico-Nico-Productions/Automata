package pro.piconico.automata.bot.job;

import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

public class BotJobAssignment {
    public static final Codec<BotJobAssignment> CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(BotJob.CODEC.fieldOf("job").forGetter(jobAssignment -> jobAssignment.job),
                            Uuids.INT_STREAM_CODEC.optionalFieldOf("bot_uuid").forGetter(BotJobAssignment::getAssignedBot))
                    .apply(instance, BotJobAssignment::new));

    private Optional<UUID> assignedBot;

    public final BotJob job;

    public BotJobAssignment(BotJob job, Optional<UUID> assignedBot) {
        this.job = job;
        this.assignedBot = assignedBot;
    }

    public BotJobAssignment(BotJob job) {
        this(job, Optional.empty());
    }

    public boolean isAssigned() {
        return assignedBot.isPresent();
    }

    public Optional<UUID> getAssignedBot() {
        return assignedBot;
    }

    public void setAssignedBot(Optional<UUID> assignedBot) {
        this.assignedBot = assignedBot;
    }
}
