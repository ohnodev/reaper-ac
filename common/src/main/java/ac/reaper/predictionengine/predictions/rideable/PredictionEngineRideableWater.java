package ac.reaper.predictionengine.predictions.rideable;

import ac.reaper.player.ReaperPlayer;
import ac.reaper.predictionengine.predictions.PredictionEngineWater;
import ac.reaper.utils.data.VectorData;
import ac.reaper.utils.math.ReaperMath;
import ac.reaper.utils.math.Vector3dm;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class PredictionEngineRideableWater extends PredictionEngineWater {
    protected final Vector3dm movementVector;

    @Override
    public void addJumpsToPossibilities(ReaperPlayer player, Set<VectorData> existingVelocities) {
        PredictionEngineRideableUtils.handleJumps(player, existingVelocities);
    }

    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(ReaperPlayer player, Set<VectorData> possibleVectors, float speed) {
        return PredictionEngineRideableUtils.applyInputsToVelocityPossibilities(this, movementVector, player, possibleVectors, speed);
    }

    @Override
    public Vector3dm getMovementResultFromInput(ReaperPlayer player, Vector3dm inputVector, float flyingSpeed, float yRot) {
        float yRotRadians = ReaperMath.radians(yRot);
        float sin = player.trigHandler.sin(yRotRadians);
        float cos = player.trigHandler.cos(yRotRadians);

        double xResult = inputVector.getX() * cos - inputVector.getZ() * sin;
        double zResult = inputVector.getZ() * cos + inputVector.getX() * sin;

        return new Vector3dm(xResult * flyingSpeed, inputVector.getY() * flyingSpeed, zResult * flyingSpeed);
    }

}
