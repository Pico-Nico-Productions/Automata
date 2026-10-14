package pro.piconico.automata.bot.job;

import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

public class BotJobAssignment {
    public static final Codec<BotJobAssignment> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
            BotJob.CODEC.fieldOf("job").forGetter(jobAssignment -> jobAssignment.job), //
            Uuids.INT_STREAM_CODEC.fieldOf("team_uuid").forGetter(jobAssignment -> jobAssignment.teamUuid), //
            Uuids.INT_STREAM_CODEC.optionalFieldOf("bot_uuid").forGetter(BotJobAssignment::getAssignedBot)) //
            .apply(instance, BotJobAssignment::new));

    public final BotJob job;
    public final UUID teamUuid;
    private Optional<UUID> assignedBot;

    public BotJobAssignment(BotJob job, UUID teamUuid, Optional<UUID> assignedBot) {
        this.job = job;
        this.teamUuid = teamUuid;
        this.assignedBot = assignedBot;
    }

    public BotJobAssignment(BotJobAssignment original) {
        this(original.job, original.teamUuid, original.assignedBot);
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
