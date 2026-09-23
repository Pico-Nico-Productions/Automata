package pro.piconico.automata.bot.device;

import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import pro.piconico.automata.component.TeamComponent;
import pro.piconico.automata.network.message.BotDeviceTeamChangeMessage;
import pro.piconico.automata.registry.AutomataBotDevices;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.screen.ItemBotDeviceScreenHandler;

public class ItemBotDevice implements BotDevice<ItemStack> {
    public record Id(int slot) implements BotDevice.Id {
        public static final MapCodec<Id> CODEC = RecordCodecBuilder
                .mapCodec(instance -> instance.group(Codec.INT.fieldOf("slot").forGetter(Id::slot)).apply(instance, Id::new));

        @Override
        public BotDeviceType<?, ?> getType() {
            return AutomataBotDevices.ITEM;
        }
    }

    public final PlayerEntity player;
    private final int slot;

    public ItemBotDevice(PlayerEntity player, ItemStack stack) {
        int slot = player.getInventory().getSlotWithStack(stack);

        if (slot == -1 || stack.isEmpty())
            throw new IllegalArgumentException(ItemBotDevice.class.getSimpleName() + "'s stack must be in player's inventory and can't be empty");

        this.player = player;
        this.slot = slot;
    }

    private ItemStack getStack() {
        return player.getInventory().getStack(slot);
    }

    public Item getItem() {
        return getStack().getItem();
    }

    @Override
    public Text getDisplayName() {
        return getStack().getName();
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ItemBotDeviceScreenHandler(syncId, playerInventory, getStack());
    }

    @Override
    public ItemStack getScreenOpeningData(ServerPlayerEntity player) {
        return getStack();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof ItemBotDevice itemDevice))
            return false;

        return ItemStack.areItemsAndComponentsEqual(getStack(), itemDevice.getStack());
    }

    @Override
    public BotDevice.Id getId() {
        return new Id(slot);
    }

    @Override
    public Optional<UUID> getTeamUuid() {
        return TeamComponent.get(getStack());
    }

    @Override
    public boolean setTeamUuid(Optional<UUID> teamUuid) {
        Optional<UUID> oldTeamUuid = getTeamUuid();
        if (oldTeamUuid.equals(teamUuid))
            return false;

        TeamComponent.set(getStack(), teamUuid);

        BotDevice.invokeTeamChanged(player.getEntityWorld(), this, oldTeamUuid);
        if (player instanceof ServerPlayerEntity serverPlayer) {
            AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(serverPlayer).send(new BotDeviceTeamChangeMessage(getId(), teamUuid));
        }

        return true;
    }

    public static Optional<ItemBotDevice> resolve(PlayerEntity player, Id deviceId) {
        ItemStack stack = player.getInventory().getStack(deviceId.slot);

        if (stack.isEmpty())
            return Optional.empty();

        if (player.currentScreenHandler instanceof BotDeviceScreenHandler deviceScreenHandler && deviceScreenHandler.device instanceof ItemBotDevice itemDevice
                && !ItemStack.areItemsAndComponentsEqual(itemDevice.getStack(), stack)) {
            return Optional.empty();
        }

        return Optional.of(new ItemBotDevice(player, stack));
    }
}
