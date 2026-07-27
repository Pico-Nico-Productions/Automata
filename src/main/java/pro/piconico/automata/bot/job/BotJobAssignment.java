package pro.piconico.automata.bot.job;

import java.util.Optional;
import java.util.UUID;

public interface BotJobAssignment {
    public BotJob getJob();

    public Optional<UUID> getAssignedBot();

    public boolean isAssigned();

    public void setAssignedBot(Optional<UUID> assignedBot);
}
