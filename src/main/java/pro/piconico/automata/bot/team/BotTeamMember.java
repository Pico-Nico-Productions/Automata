package pro.piconico.automata.bot.team;

import java.util.EnumSet;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;

public class BotTeamMember {
    private enum BotTeamPermission {
        READ, UPDATE, DELETE;
    }

    public enum BotTeamRole {
        USER(EnumSet.of(BotTeamPermission.READ)),
        ADMIN(EnumSet.of(BotTeamPermission.READ, BotTeamPermission.UPDATE)),
        OWNER(EnumSet.allOf(BotTeamPermission.class));

        public static final Codec<BotTeamRole> CODEC = Codec.INT.xmap(ordinal -> BotTeamRole.values()[ordinal], role -> role.ordinal());

        public final EnumSet<BotTeamPermission> permissions;

        private BotTeamRole(EnumSet<BotTeamPermission> permissions) {
            this.permissions = permissions;
        }
    }

    public static final Codec<BotTeamMember> CODEC = RecordCodecBuilder.create(instance -> instance.group( //
        Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(member -> member.uuid), //
        Codec.STRING.fieldOf("cachedName").forGetter(BotTeamMember::getCachedName), //
        BotTeamRole.CODEC.fieldOf("role").forGetter(member -> member.role) //
        ).apply(instance, BotTeamMember::new));

    private String cachedName;
    private BotTeamRole role;

    public final UUID uuid;

    private BotTeamMember(UUID uuid, String cachedName, BotTeamRole role) {
        this.uuid = uuid;
        this.cachedName = cachedName;
        this.role = role;
    }

    BotTeamMember(PlayerEntity player, BotTeamRole role) {
        this(player.getUuid(), player.getStringifiedName(), role);
    }

    public String getCachedName() {
        return cachedName;
    }

    public String getName(MinecraftServer server) {
        ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(uuid);

        if (serverPlayer != null && !serverPlayer.getStringifiedName().equals(cachedName)) {
            cachedName = serverPlayer.getStringifiedName();
        }

        return cachedName;
    }

    public BotTeamRole getRole() {
        return role;
    }

    void setRole(BotTeamRole role) {
        this.role = role;
    }
}
