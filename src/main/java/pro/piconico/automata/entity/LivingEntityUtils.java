package pro.piconico.automata.entity;

import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class LivingEntityUtils {
    public static boolean isHolding(LivingEntity livingEntity, Predicate<ItemStack> stackPredicate) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = livingEntity.getStackInHand(hand);

            if (!stackPredicate.test(stack))
                continue;

            return true;
        }

        return false;
    }

    public static SequencedMap<Hand, ItemStack> getHeldStacks(LivingEntity livingEntity, Predicate<ItemStack> stackPredicate) {
        SequencedMap<Hand, ItemStack> stacks = new LinkedHashMap<>(Hand.values().length);

        for (Hand hand : Hand.values()) {
            ItemStack stack = livingEntity.getStackInHand(hand);

            if (!stackPredicate.test(stack))
                continue;

            stacks.put(hand, stack);
        }

        return stacks;
    }
}
