package pro.piconico.automata.entity;

import java.util.LinkedHashMap;
import java.util.SequencedMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class LivingEntityUtils {
    public static boolean isHolding(LivingEntity livingEntity, Item item) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = livingEntity.getStackInHand(hand);

            if (!stack.isOf(item))
                continue;

            return true;
        }

        return false;
    }

    public static SequencedMap<Hand, ItemStack> getHeldStacks(LivingEntity livingEntity, Item item) {
        SequencedMap<Hand, ItemStack> stacks = new LinkedHashMap<>(Hand.values().length);

        for (Hand hand : Hand.values()) {
            ItemStack stack = livingEntity.getStackInHand(hand);

            if (!stack.isOf(item))
                continue;

            stacks.put(hand, stack);
        }

        return stacks;
    }
}
