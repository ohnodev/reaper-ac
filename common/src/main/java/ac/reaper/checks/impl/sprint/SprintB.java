package ac.reaper.checks.impl.sprint;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.PostPredictionCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.PredictionComplete;
import ac.reaper.utils.enums.Pose;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

import java.util.Collections;

@CheckData(name = "SprintB", description = "Sprinting while sneaking or crawling", setback = 5, experimental = true)
public class SprintB extends Check implements PostPredictionCheck {
    public SprintB(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (player.isSlowMovement && player.sneakingSpeedMultiplier < 0.8f && predictionComplete.isChecked()) {
            ClientVersion version = player.getClientVersion();

            // https://bugs.mojang.com/browse/MC-152728
            if (version.isNewerThanOrEquals(ClientVersion.V_1_14_2) && version != ClientVersion.V_1_21_4) {
                return;
            }

            // https://github.com/ReaperAnticheat/Reaper/issues/1932
            if (version.isNewerThanOrEquals(ClientVersion.V_1_14) && player.wasFlying && player.lastPose == Pose.FALL_FLYING && !player.isGliding) {
                return;
            }

            // https://github.com/ReaperAnticheat/Reaper/issues/1948
            if (version == ClientVersion.V_1_21_4 && (Collections.max(player.uncertaintyHandler.pistonX) != 0
                    || Collections.max(player.uncertaintyHandler.pistonY) != 0
                    || Collections.max(player.uncertaintyHandler.pistonZ) != 0)) {
                return;
            }

            if (player.isSprinting && (!player.wasTouchingWater || version.isOlderThan(ClientVersion.V_1_13))) {
                flagAndAlertWithSetback();
            } else reward();
        }
    }
}
