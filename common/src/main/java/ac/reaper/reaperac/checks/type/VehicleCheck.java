package ac.reaper.reaperac.checks.type;

import ac.grim.reaperac.api.AbstractCheck;
import ac.reaper.reaperac.utils.anticheat.update.VehiclePositionUpdate;

public interface VehicleCheck extends AbstractCheck {

    void process(final VehiclePositionUpdate vehicleUpdate);
}
