package pro.piconico.automata.entity;

import java.util.LinkedHashSet;
import java.util.SequencedSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;

public class LivingEntityUtils {
    public static SequencedSet<Hand> getHandsHolding(LivingEntity livingEntity, Item item) {
        SequencedSet<Hand> hands = new LinkedHashSet<>(Hand.values().length);

        for (Hand hand : Hand.values()) {
            if (!livingEntity.getStackInHand(hand).isOf(item))
                continue;
            
            hands.add(hand);
        }

        return hands;
    }
}
