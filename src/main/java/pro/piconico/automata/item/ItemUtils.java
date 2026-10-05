package pro.piconico.automata.item;

import java.util.function.Predicate;
import net.minecraft.item.ItemStack;

public class ItemUtils {
    public record PredicateItemStack(Predicate<ItemStack> predicate, int count) {
        public PredicateItemStack(Predicate<ItemStack> predicate) {
            this(predicate, 1);
        }

        public PredicateItemStack copyWithCount(int count) {
            return new PredicateItemStack(predicate, count);
        }

        public boolean test(ItemStack stack) {
            return predicate.test(stack);
        }
    }
}
