package pro.piconico.automata.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class ConstructionBotEntity extends BotEntity {
    public ConstructionBotEntity(EntityType<? extends BotEntity> entityType, World world) {
        super(entityType, world);
    }
}
