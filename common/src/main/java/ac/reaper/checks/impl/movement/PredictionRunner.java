package ac.reaper.checks.impl.movement;

import ac.reaper.checks.Check;
import ac.reaper.checks.type.PositionCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.PositionUpdate;

public class PredictionRunner extends Check implements PositionCheck {
    public PredictionRunner(ReaperPlayer playerData) {
        super(playerData);
    }

    @Override
    public void onPositionUpdate(final PositionUpdate positionUpdate) {
        if (!player.inVehicle()) {
            player.movementCheckRunner.processAndCheckMovementPacket(positionUpdate);
        }
    }
}
