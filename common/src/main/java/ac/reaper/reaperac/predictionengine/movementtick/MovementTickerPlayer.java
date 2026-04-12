package ac.reaper.reaperac.predictionengine.movementtick;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.predictionengine.predictions.PredictionEngineLava;
import ac.reaper.reaperac.predictionengine.predictions.PredictionEngineNormal;
import ac.reaper.reaperac.predictionengine.predictions.PredictionEngineWater;
import ac.reaper.reaperac.utils.nmsutil.BlockProperties;

public class MovementTickerPlayer extends MovementTicker {
    public MovementTickerPlayer(GrimPlayer player) {
        super(player);
    }

    @Override
    public void doWaterMove(float swimSpeed, boolean isFalling, float swimFriction) {
        new PredictionEngineWater().guessBestMovement(swimSpeed, player, isFalling, player.gravity, swimFriction);
    }

    @Override
    public void doLavaMove() {
        new PredictionEngineLava().guessBestMovement(0.02F, player);
    }

    @Override
    public void doNormalMove(float blockFriction) {
        new PredictionEngineNormal().guessBestMovement(BlockProperties.getFrictionInfluencedSpeed(blockFriction, player), player);
    }
}
