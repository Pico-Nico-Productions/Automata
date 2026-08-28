package pro.piconico.automata.bot.team;

import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
    
    public static final Codec<BotTeam> CODEC = RecordCodecBuilder.create(instance -> instance.group(Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(team -> team.UUID), Codec.STRING.fieldOf("name").forGetter(BotTeam::getName)).apply(instance, BotTeam::new));

    private String name;

    public final UUID UUID;

    private BotTeam(UUID uuid, String name) {
        this.UUID = uuid;
        this.name = name;
    }

    public BotTeam(String name) {
        UUID = java.util.UUID.randomUUID();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Environment(EnvType.SERVER)
    public void setName(String name) {
        this.name = name;
        MUTATED.invoker().onMutate(this);
    }
}
