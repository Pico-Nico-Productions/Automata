package pro.piconico.automata.bot.device;

import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import pro.piconico.automata.component.TeamComponent;
import pro.piconico.automata.screen.ItemBotDeviceScreenHandler;

public class ItemBotDevice implements BotDevice<ItemStack> {
    private final ItemStack stack;

    public ItemBotDevice(ItemStack stack) {
        if (stack.isEmpty())
            throw new IllegalArgumentException(ItemBotDevice.class.getSimpleName() + "'s stack can't be empty");

        this.stack = stack;
    }

    public Item getItem() {
        return stack.getItem();
    }

    @Override
    public Optional<UUID> getTeamUuid() {
        return TeamComponent.get(stack);
    }

    @Override
    public boolean setTeamUuid(Optional<UUID> teamUuid) {
        Optional<UUID> oldTeamUuid = getTeamUuid();
        if (oldTeamUuid.equals(teamUuid))
            return false;

        TeamComponent.set(stack, teamUuid);

        if (stack.getHolder() != null && stack.getHolder().getEntityWorld().isClient())
            return true;

        TEAM_CHANGED.invoker().onChanged(this, oldTeamUuid);

        return true;
    }

    @Override
    public Text getDisplayName() {
        return stack.getName();
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ItemBotDeviceScreenHandler(syncId, playerInventory, stack);
    }

    @Override
    public ItemStack getScreenOpeningData(ServerPlayerEntity player) {
        return stack;
    }
}
