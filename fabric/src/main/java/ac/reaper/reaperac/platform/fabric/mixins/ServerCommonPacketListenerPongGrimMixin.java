package ac.reaper.reaperac.platform.fabric.mixins;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.platform.fabric.utils.LegacyLinkCompat;
import ac.reaper.reaperac.player.GrimPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla registers {@link ServerboundPongPacket} as {@code CommonPacketTypes.SERVERBOUND_PONG} inside the PLAY
 * protocol bundle (see decompiled {@code GameProtocols} for 26.1.x / 26.2). {@link ServerCommonPacketListenerImpl#handlePong}
 * is a no-op; Grim relied on PacketEvents classifying the same bytes as {@code Play.Client.PONG}. On LegacyLink
 * sessions PE mapping can miss, so we advance Grim's transaction clock from the vanilla packet path.
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerPongGrimMixin {

    @Shadow
    protected Connection connection;

    @Inject(method = "handlePong", at = @At("HEAD"))
    private void reaper$grimRecordLegacyPong(ServerboundPongPacket packet, CallbackInfo ci) {
        Channel nettyChannel = ((ConnectionChannelAccessor) (Object) connection).reaper$getNettyChannel();
        if (nettyChannel == null || !LegacyLinkCompat.isLegacyNettyChannel(nettyChannel)) {
            return;
        }
        User user = PacketEvents.getAPI().getProtocolManager().getUser(nettyChannel);
        if (user == null) {
            return;
        }
        GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(user);
        if (player == null) {
            return;
        }
        int id = packet.getId();
        if (id != (short) id) {
            return;
        }
        short shortId = (short) id;
        // If PacketEvents already consumed this pong on the Netty thread, addTransactionResponse is a no-op; do not
        // touch lastTransactionPacketWasValid here (PE may have just set it true).
        if (player.addTransactionResponse(shortId)) {
            player.packetStateData.lastTransactionPacketWasValid = true;
        }
    }
}
