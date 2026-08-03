package pro.piconico.automata.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import pro.piconico.automata.bot.BotType;
import pro.piconico.automata.registry.AutomataBots;

public class ConstructionBotEntity extends BotEntity {
    public ConstructionBotEntity(EntityType<? extends BotEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public BotType getBotType() {
        return AutomataBots.CONSTRUCTION_BOT;
    }
}
