package ac.reaper.reaperac.checks.impl.vehicle;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.player.GrimPlayer;

@CheckData(name = "VehicleC")
public class VehicleC extends Check {
    public VehicleC(GrimPlayer player) {
        super(player);
    }
}
