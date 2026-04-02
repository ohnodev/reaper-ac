package ac.reaper.checks.impl.vehicle;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.PacketCheck;
import ac.reaper.player.ReaperPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "VehicleB", description = "Claimed to be in a vehicle while not in a vehicle")
public class VehicleB extends Check implements PacketCheck {
    public VehicleB(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
            if (!player.inVehicle()) {
                if (flagAndAlert() && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
