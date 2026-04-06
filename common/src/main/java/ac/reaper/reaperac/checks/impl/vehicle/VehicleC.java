package ac.reaper.reaperac.checks.impl.vehicle;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PacketCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "VehicleC")
public class VehicleC extends Check implements PacketCheck {
    public VehicleC(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // Intentionally inert for now; hook retained so this check has a packet entrypoint.
    }
}
