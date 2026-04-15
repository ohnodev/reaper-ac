package ac.reaper.reaperac.platform.fabric.mixins;

import ac.reaper.reaperac.platform.fabric.utils.LegacyLinkCompat;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.util.PacketEventsImplHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * For LegacyLink sessions the wire is 26.1 but {@link PacketEventsImplHelper#handlePacket} defaults to
 * {@code autoProtocolTranslation=true}, which maps packet IDs using the <em>server</em> protocol (26.2).
 * That mis-identifies serverbound <em>play</em> packets and breaks Grim movement simulation. Disable
 * auto-translation in {@link ConnectionState#PLAY} only: handshake/login/configuration still need the
 * default server mapping or the ByteBuf is mis-read and the vanilla decoder disconnects the client.
 */
@Mixin(targets = "io.github.retrooper.packetevents.handler.PacketDecoder")
public abstract class PeDecoderLegacyInboundMixin {

    @Redirect(
            method = "decode",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/retrooper/packetevents/util/PacketEventsImplHelper;handlePacket(Ljava/lang/Object;Lcom/github/retrooper/packetevents/protocol/player/User;Ljava/lang/Object;Ljava/lang/Object;ZLcom/github/retrooper/packetevents/protocol/PacketSide;)Lcom/github/retrooper/packetevents/event/ProtocolPacketEvent;"
            )
    )
    private static ProtocolPacketEvent reaper$legacyAwareInboundDecode(
            Object channel,
            User user,
            Object player,
            Object buffer,
            boolean autoProtocolTranslation,
            PacketSide side
    ) throws Exception {
        boolean auto = autoProtocolTranslation;
        if (auto
                && LegacyLinkCompat.isLegacyNettyChannel(channel)
                && user != null
                && user.getDecoderState() == ConnectionState.PLAY) {
            auto = false;
        }
        return PacketEventsImplHelper.handlePacket(channel, user, player, buffer, auto, side);
    }
}
