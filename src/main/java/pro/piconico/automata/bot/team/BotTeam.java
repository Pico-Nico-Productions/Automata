package pro.piconico.automata.bot.team;

import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Uuids;

public class BotTeam {
    @FunctionalInterface
    public interface Mutate {
        void onMutate(BotTeam team);
    }

    public static final Event<Mutate> MUTATED = EventFactory.createArrayBacked(Mutate.class, callbacks -> (team) -> {
        for (Mutate callback : callbacks) {
            callback.onMutate(team);
        }
    });

    public static final Codec<BotTeam> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Codec.STRING.fieldOf("name").forGetter(BotTeam::getName), Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(team -> team.UUID))
            .apply(instance, BotTeam::new));

    public static final String EMPTY_UUID = " ";

    private String name;

    public final UUID UUID;

    public BotTeam(String name, UUID UUID) {
        this.name = name;
        this.UUID = UUID;
    }

    public BotTeam(String name) {
        this(name, java.util.UUID.randomUUID());
    }

    public void set(BotTeam team) {
        if (!this.UUID.equals(team.UUID) || this.equals(team))
            return;

        name = team.name;

        MUTATED.invoker().onMutate(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name.equals(name))
            return;

        this.name = name;
        MUTATED.invoker().onMutate(this);
    }
}
