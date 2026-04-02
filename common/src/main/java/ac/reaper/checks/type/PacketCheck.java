package ac.reaper.checks.type;

import ac.reaper.api.AbstractCheck;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;

public interface PacketCheck extends AbstractCheck {
    default void onPacketReceive(final PacketReceiveEvent event) {
    }

    default void onPacketSend(final PacketSendEvent event) {
    }
}
