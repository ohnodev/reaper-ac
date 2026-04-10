package ac.reaper.reaperac.checks.impl.prediction;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PostPredictionCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.PredictionComplete;
import ac.reaper.reaperac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.reaper.reaperac.utils.nmsutil.Collisions;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "Phase", setback = 1, decay = 0.005)
public class Phase extends Check implements PostPredictionCheck {
    private SimpleCollisionBox oldBB;

    public Phase(GrimPlayer player) {
        super(player);
        oldBB = player.boundingBox;
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!player.getSetbackTeleportUtil().blockOffsets && !predictionComplete.getData().isTeleport() && predictionComplete.isChecked()) { // Not falling through world
            SimpleCollisionBox newBB = player.boundingBox;

            List<SimpleCollisionBox> boxes = new ArrayList<>();
            Collisions.getCollisionBoxes(player, newBB, boxes, false);

            for (SimpleCollisionBox box : boxes) {
                if (newBB.isIntersected(box) && !oldBB.isIntersected(box)) {
                    flagAndAlertWithSetback();
                    return;
                }
            }
        }

        oldBB = player.boundingBox;
        reward();
    }
}
