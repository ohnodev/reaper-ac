package ac.reaper.checks.impl.flight;

import ac.reaper.checks.Check;
import ac.reaper.checks.type.PacketCheck;
import ac.reaper.player.ReaperPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

// This check catches 100% of cheaters.
public class FlightA extends Check implements PacketCheck {
    public FlightA(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // If the player sends a flying packet, but they aren't flying, then they are cheating.
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && !player.isFlying) {
            flag();
        }
    }
}
