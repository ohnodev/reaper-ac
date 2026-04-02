package ac.reaper.checks.type;

import ac.reaper.api.AbstractCheck;
import ac.reaper.utils.anticheat.update.VehiclePositionUpdate;

public interface VehicleCheck extends AbstractCheck {

    void process(final VehiclePositionUpdate vehicleUpdate);
}
