package ac.reaper.checks.impl.sprint;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.PostPredictionCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(name = "SprintF", description = "Sprinting while gliding", experimental = true)
public class SprintF extends Check implements PostPredictionCheck {
    public SprintF(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (player.wasGliding && player.isGliding && player.getClientVersion() == ClientVersion.V_1_21_4) {
            if (player.isSprinting) {
                flagAndAlertWithSetback();
            } else {
                reward();
            }
        }
    }
}
