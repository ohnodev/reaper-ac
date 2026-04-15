package ac.reaper.reaperac.platform.fabric.mixins;

import ac.reaper.reaperac.platform.fabric.utils.LegacyLinkPeVanillaOutbound;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code ProtocolManager.sendPacket(channel, wrapper)} is a default interface method, so it cannot be
 * mixed into {@code FabricProtocolManager}. Intercept on {@link User} instead (real methods).
 */
@Mixin(User.class)
public abstract class PeUserLegacyOutboundMixin {

    @Shadow
    public abstract Object getChannel();

    @Inject(method = "sendPacket(Lcom/github/retrooper/packetevents/wrapper/PacketWrapper;)V", at = @At("HEAD"), cancellable = true)
    private void reaper$legacyOutboundSend(PacketWrapper<?> wrapper, CallbackInfo ci) {
        if (LegacyLinkPeVanillaOutbound.tryRoute(getChannel(), wrapper)) {
            ci.cancel();
        }
    }

    @Inject(method = "sendPacketSilently(Lcom/github/retrooper/packetevents/wrapper/PacketWrapper;)V", at = @At("HEAD"), cancellable = true)
    private void reaper$legacyOutboundSendSilently(PacketWrapper<?> wrapper, CallbackInfo ci) {
        if (LegacyLinkPeVanillaOutbound.tryRoute(getChannel(), wrapper)) {
            ci.cancel();
        }
    }

    @Inject(method = "writePacket(Lcom/github/retrooper/packetevents/wrapper/PacketWrapper;)V", at = @At("HEAD"), cancellable = true)
    private void reaper$legacyOutboundWrite(PacketWrapper<?> wrapper, CallbackInfo ci) {
        if (LegacyLinkPeVanillaOutbound.tryRoute(getChannel(), wrapper)) {
            ci.cancel();
        }
    }

    @Inject(method = "writePacketSilently(Lcom/github/retrooper/packetevents/wrapper/PacketWrapper;)V", at = @At("HEAD"), cancellable = true)
    private void reaper$legacyOutboundWriteSilently(PacketWrapper<?> wrapper, CallbackInfo ci) {
        if (LegacyLinkPeVanillaOutbound.tryRoute(getChannel(), wrapper)) {
            ci.cancel();
        }
    }
}
