package ac.reaper.predictionengine.predictions.rideable;

import ac.reaper.player.ReaperPlayer;
import ac.reaper.predictionengine.predictions.PredictionEngineWaterLegacy;
import ac.reaper.utils.data.VectorData;
import ac.reaper.utils.math.Vector3dm;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class PredictionEngineRideableWaterLegacy extends PredictionEngineWaterLegacy {
    private final Vector3dm movementVector;

    @Override
    public void addJumpsToPossibilities(ReaperPlayer player, Set<VectorData> existingVelocities) {
        PredictionEngineRideableUtils.handleJumps(player, existingVelocities);
    }

    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(ReaperPlayer player, Set<VectorData> possibleVectors, float speed) {
        return PredictionEngineRideableUtils.applyInputsToVelocityPossibilities(movementVector, player, possibleVectors, speed);
    }
}
