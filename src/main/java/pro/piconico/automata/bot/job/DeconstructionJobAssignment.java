package pro.piconico.automata.bot.job;

import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

public class DeconstructionJobAssignment implements BotJobAssignment {
    public static final Codec<DeconstructionJobAssignment> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(DeconstructionJob.CODEC.fieldOf("job").forGetter(jobAssignment -> jobAssignment.job),
                    Uuids.INT_STREAM_CODEC.optionalFieldOf("bot_uuid").forGetter(jobAssignment -> jobAssignment.assignedBot))
            .apply(instance, DeconstructionJobAssignment::new));

    private Optional<UUID> assignedBot;

    public final DeconstructionJob job;

    public DeconstructionJobAssignment(DeconstructionJob job, Optional<UUID> assignedBot) {
        this.job = job;
        this.assignedBot = assignedBot;
    }

    public DeconstructionJobAssignment(DeconstructionJob job) {
        this(job, Optional.empty());
    }

    @Override
    public BotJob getJob() {
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
