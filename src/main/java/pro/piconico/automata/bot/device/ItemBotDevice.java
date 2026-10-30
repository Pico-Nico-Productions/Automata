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
import net.minecraft.util.math.BlockPos;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeamMember.BotTeamPermission;
import pro.piconico.automata.component.TeamComponent;
import pro.piconico.automata.network.message.BotDeviceTeamChangeMessage;
import pro.piconico.automata.registry.AutomataBotDevices;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;
import pro.piconico.automata.screen.ItemBotDeviceScreenHandler;
import pro.piconico.automata.world.BotTeamPersistentState;

public class ItemBotDevice implements BotDevice<ItemStack> {
    public record ItemId(int slot) implements BotDevice.Id {
        public static final MapCodec<ItemId> CODEC = RecordCodecBuilder
                .mapCodec(instance -> instance.group(Codec.INT.fieldOf("slot").forGetter(ItemId::slot)).apply(instance, ItemId::new));

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

    public BlockPos getPos() {
        return player.getBlockPos();
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
    public BotDevice.Id getDeviceId() {
        return new ItemId(slot);
    }

    @Override
    public boolean canUse(PlayerEntity player, boolean sendMessage) {
        return this.player == player && BotDevice.super.canUse(player, sendMessage);
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
            AutomataMessages.BOT_DEVICE_CHANNEL.serverHandle(serverPlayer).send(new BotDeviceTeamChangeMessage(getDeviceId(), teamUuid));
        }

        return true;
    }

    public boolean hasPermission(BotTeamPermission permission, boolean sendMessage) {
        if (player.getEntityWorld().isClient())
            return true;

        ItemStack stack = getStack();
        Optional<BotTeam> team = TeamComponent.get(stack).map(uuid -> BotTeamPersistentState.getTeam(uuid).orElse(null));

        if (team.isEmpty()) {
            TeamComponent.set(stack, Optional.empty());
            if (sendMessage) {
                player.sendMessage(AutomataTexts.getBotTeamEmpty(), true);
            }

            return false;
        }

        if (!team.get().hasMemberWith(player, permission)) {
            if (sendMessage) {
                player.sendMessage(AutomataTexts.getBotTeamPermissionMissing(player.getStringifiedName(), team.get()), true);
            }

            return false;
        }

        return true;
    }

    public final boolean hasPermission(BotTeamPermission permission) {
        return hasPermission(permission, false);
    }

    public static Optional<ItemBotDevice> resolve(PlayerEntity player, ItemId deviceId) {
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
