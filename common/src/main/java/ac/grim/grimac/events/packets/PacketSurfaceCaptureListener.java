package ac.grim.grimac.events.packets;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;

/**
 * Disabled intentionally: packet-surface capture was used only for migration
 * experiments and is not part of production packet handling.
 */
public final class PacketSurfaceCaptureListener extends PacketListenerAbstract {
    public PacketSurfaceCaptureListener() {
        super(PacketListenerPriority.MONITOR);
    }

    public static boolean isEnabled() {
        return false;
    }

    public void close() {
        // No-op: capture is disabled.
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // Capture disabled.
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        // Capture disabled.
    }
}
