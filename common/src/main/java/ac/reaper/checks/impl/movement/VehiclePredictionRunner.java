package ac.reaper.checks.impl.movement;

import ac.reaper.checks.Check;
import ac.reaper.checks.type.VehicleCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.PositionUpdate;
import ac.reaper.utils.anticheat.update.VehiclePositionUpdate;

public class VehiclePredictionRunner extends Check implements VehicleCheck {
    public VehiclePredictionRunner(ReaperPlayer playerData) {
        super(playerData);
    }

    @Override
    public void process(final VehiclePositionUpdate vehicleUpdate) {
        // Vehicle onGround = false always
        // We don't do vehicle setbacks because vehicle netcode sucks.
        player.movementCheckRunner.processAndCheckMovementPacket(new PositionUpdate(vehicleUpdate.from(), vehicleUpdate.to(), false, null, null, vehicleUpdate.isTeleport()));
    }
}
