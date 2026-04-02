package ac.reaper.checks.impl.vehicle;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.player.ReaperPlayer;

@CheckData(name = "VehicleC")
public class VehicleC extends Check {
    public VehicleC(ReaperPlayer player) {
        super(player);
    }
}
