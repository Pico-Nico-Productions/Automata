package pro.piconico.automata.bot.job;

import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

public class BotJobAssignment {
    public static final Codec<BotJobAssignment> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
            BotJob.CODEC.fieldOf("job").forGetter(jobAssignment -> jobAssignment.JOB), //
            Uuids.INT_STREAM_CODEC.fieldOf("team_uuid").forGetter(jobAssignment -> jobAssignment.TEAM_UUID), //
            Uuids.INT_STREAM_CODEC.optionalFieldOf("bot_uuid").forGetter(BotJobAssignment::getAssignedBot)) //
            .apply(instance, BotJobAssignment::new));

    private Optional<UUID> assignedBot;

    public final BotJob JOB;
    public final UUID TEAM_UUID;

    public BotJobAssignment(BotJob job, UUID teamUuid, Optional<UUID> assignedBot) {
        JOB = job;
        TEAM_UUID = teamUuid;
        this.assignedBot = assignedBot;
    }

    public BotJobAssignment(BotJob job, UUID teamUuid) {
        this(job, teamUuid, Optional.empty());
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
