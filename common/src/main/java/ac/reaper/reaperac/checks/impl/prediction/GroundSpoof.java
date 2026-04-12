package ac.reaper.reaperac.checks.impl.prediction;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PostPredictionCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.protocol.player.GameMode;

@CheckData(name = "GroundSpoof", setback = 10, decay = 0.01)
public class GroundSpoof extends Check implements PostPredictionCheck {

    public GroundSpoof(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        // Exemptions
        if (player.gamemode == GameMode.SPECTATOR) return; // Don't check players in spectator
        // And don't check this long list of ground exemptions
        if (player.exemptOnGround() || !predictionComplete.isChecked()) return;
        // Don't check if the player was on a ghost block
        if (player.getSetbackTeleportUtil().blockOffsets) return;
        // Viaversion sends wrong ground status... (doesn't matter but is annoying)
        if (player.packetStateData.lastPacketWasTeleport) return;

        if (player.clientClaimsLastOnGround != player.onGround) {
            flagAndAlertWithSetback("claimed " + player.clientClaimsLastOnGround);
            player.checkManager.getNoFall().flipPlayerGroundStatus = true;
        }
    }
}
