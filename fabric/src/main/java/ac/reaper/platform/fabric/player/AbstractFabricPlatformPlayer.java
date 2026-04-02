package ac.reaper.platform.fabric.player;

import ac.reaper.platform.api.player.BlockTranslator;
import ac.reaper.platform.api.entity.ReaperEntity;
import ac.reaper.platform.api.player.PlatformInventory;
import ac.reaper.platform.api.player.PlatformPlayer;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.entity.AbstractFabricReaperEntity;
import ac.reaper.platform.fabric.utils.PolymerHook;
import ac.reaper.platform.fabric.utils.convert.FabricConversionUtil;
import ac.reaper.utils.common.arguments.CommonReaperArguments;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class AbstractFabricPlatformPlayer extends AbstractFabricReaperEntity implements PlatformPlayer {
    protected volatile ServerPlayer fabricPlayer;
    protected final AbstractFabricPlatformInventory inventory;
    private final @Nullable User user;
    @Getter private final BlockTranslator blockTranslator;

    public AbstractFabricPlatformPlayer(ServerPlayer player) {
        super(player);
        this.fabricPlayer = player;
        this.inventory = ReaperACFabricLoaderPlugin.LOADER.getPlatformPlayerFactory().getPlatformInventory(this);
        if (CommonReaperArguments.USE_CHAT_FAST_BYPASS.value()) {
            Object channel = PacketEvents.getAPI().getProtocolManager().getChannel(fabricPlayer.getUUID());
            this.user = PacketEvents.getAPI().getProtocolManager().getUser(channel);
        } else {
            this.user = null;
        }

        this.blockTranslator = PolymerHook.createTranslator(this.fabricPlayer);
    }

    @Override
    public void kickPlayer(String textReason) {
        fabricPlayer.connection.disconnect(ReaperACFabricLoaderPlugin.LOADER.getFabricMessageUtils().textLiteral(textReason));
    }

    @Override
    public boolean isSneaking() {
        return fabricPlayer.isShiftKeyDown();
    }

    @Override
    public void setSneaking(boolean isSneaking) {
        fabricPlayer.setShiftKeyDown(isSneaking);
    }

    @Override
    public boolean hasPermission(String permission) {
        return getSender().hasPermission(permission);
    }

    @Override
    public boolean hasPermission(String permission, boolean defaultIfUnset) {
        return getSender().hasPermission(permission, defaultIfUnset);
    }

    @Override
    public void sendMessage(String message) {
        if (CommonReaperArguments.USE_CHAT_FAST_BYPASS.value() && user != null) {
            user.sendMessage(message);
        } else {
            fabricPlayer.sendSystemMessage(ReaperACFabricLoaderPlugin.LOADER.getFabricMessageUtils().textLiteral(message), false);
        }
    }

    @Override
    public void sendMessage(Component message) {
        if (CommonReaperArguments.USE_CHAT_FAST_BYPASS.value() && user != null) {
            user.sendMessage(message);
        } else {
            fabricPlayer.sendSystemMessage(ReaperACFabricLoaderPlugin.LOADER.getFabricConversionUtil().toNativeText(message), false);
        }
    }

    @Override
    public boolean isOnline() {
        return !fabricPlayer.hasDisconnected();
    }

    @Override
    public String getName() {
        return fabricPlayer.getName().getString();
    }

    @Override
    public void updateInventory() {
        fabricPlayer.containerMenu.broadcastChanges();
    }

    @Override
    public Vector3d getPosition() {
        return new Vector3d(fabricPlayer.getX(), fabricPlayer.getY(), fabricPlayer.getZ());
    }

    @Override
    public PlatformInventory getInventory() {
        return inventory;
    }

    @Override
    public ReaperEntity getVehicle() {
        Entity vehicle = fabricPlayer.getVehicle();
        return vehicle != null ? ReaperACFabricLoaderPlugin.LOADER.getPlatformPlayerFactory().getPlatformEntity(vehicle) : null;
    }

    @Override
    public GameMode getGameMode() {
        return FabricConversionUtil.fromFabricGameMode(fabricPlayer.gameMode.getGameModeForPlayer());
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        fabricPlayer.setGameMode(FabricConversionUtil.toFabricGameMode(gameMode));
    }

    @Override
    public UUID getUniqueId() {
        return fabricPlayer.getUUID();
    }

    @Override
    public boolean isExternalPlayer() {
        return false;
    }

    @Override
    public void sendPluginMessage(String channelName, byte[] byteArray) {
        // You might want to use Fabric's networking system here
//        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(
//                Identifier.of(channelName),
//                new PacketByteBuf(Unpooled.wrappedBuffer(byteArray))
//        );
//        fabricPlayer.networkHandler.sendPacket(packet);
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceNativePlayer(Object nativePlayerObject) {
        this.fabricPlayer = (ServerPlayer) nativePlayerObject;
    }

    @Override
    public @NotNull ServerPlayer getNative() {
        return this.fabricPlayer;
    }

    @Override
    public boolean isDead() {
        return fabricPlayer.isDeadOrDying();
    }
}
