package ac.reaper.checks.impl.vehicle;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.PacketCheck;
import ac.reaper.player.ReaperPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "VehicleE", experimental = true, description = "Sent boat paddle states while not in a boat")
public class VehicleE extends Check implements PacketCheck {
    public VehicleE(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.STEER_BOAT) {
            final EntityType vehicle = player.getVehicleType();

            if (!EntityTypes.isTypeInstanceOf(vehicle, EntityTypes.BOAT)) {
                if (flagAndAlert("vehicle=" + (vehicle == null ? "null" : vehicle.getName().getKey().toLowerCase())) && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
