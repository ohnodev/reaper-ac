package ac.reaper.reaperac.checks.impl.sprint;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PostPredictionCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(name = "SprintG", description = "Sprinting while in water", experimental = true)
public class SprintG extends Check implements PostPredictionCheck {
    public SprintG(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (player.wasTouchingWater && (player.wasWasTouchingWater || player.getClientVersion() == ClientVersion.V_1_21_4)
                && !player.wasEyeInWater) {
            player.getClientVersion();
            if (player.wasLastPredictionCompleteChecked && predictionComplete.isChecked()) {
                if (player.isSprinting && !player.isSwimming) {
                    flagAndAlertWithSetback();
                } else {
                    reward();
                }
            }
        }
    }
}
