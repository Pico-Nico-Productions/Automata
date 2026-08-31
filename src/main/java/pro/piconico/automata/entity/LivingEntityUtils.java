package pro.piconico.automata.entity;

import java.util.LinkedHashSet;
import java.util.SequencedSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class LivingEntityUtils {
    public static SequencedSet<ItemStack> getHeldStacks(LivingEntity livingEntity, Item item) {
        SequencedSet<ItemStack> stacks = new LinkedHashSet<>(Hand.values().length);

        for (Hand hand : Hand.values()) {
            ItemStack stack = livingEntity.getStackInHand(hand);

            if (!stack.isOf(item))
                continue;
            
            stacks.add(stack);
        }

        return stacks;
    }
}
