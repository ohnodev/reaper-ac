package ac.reaper.checks.impl.sprint;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.PostPredictionCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(name = "SprintG", description = "Sprinting while in water", experimental = true)
public class SprintG extends Check implements PostPredictionCheck {
    public SprintG(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (player.wasTouchingWater && (player.wasWasTouchingWater || player.getClientVersion() == ClientVersion.V_1_21_4)
                && !player.wasEyeInWater && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)
                && player.wasLastPredictionCompleteChecked && predictionComplete.isChecked()) {
            if (player.isSprinting && !player.isSwimming) {
                flagAndAlertWithSetback();
            } else {
                reward();
            }
        }
    }
}
