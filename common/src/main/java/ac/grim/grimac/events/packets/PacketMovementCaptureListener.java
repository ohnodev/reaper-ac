package ac.grim.grimac.events.packets;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;

/**
 * Disabled intentionally: movement packet capture was used only for migration
 * experiments and adds avoidable runtime overhead.
 */
public final class PacketMovementCaptureListener extends PacketListenerAbstract {
    public PacketMovementCaptureListener() {
        super(PacketListenerPriority.MONITOR);
    }

    public static boolean isEnabled() {
        return false;
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
