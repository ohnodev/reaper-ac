package ac.reaper.reaperac.predictionengine;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.type.PostPredictionCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.PredictionComplete;
import ac.reaper.reaperac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntity;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public class GhostBlockDetector extends Check implements PostPredictionCheck {

    public GhostBlockDetector(GrimPlayer player) {
        super(player);
    }

    public static boolean isGhostBlock(GrimPlayer player) {
        // Player is on glitchy block (1.8 client on anvil/wooden chest)
        return player.uncertaintyHandler.isOrWasNearGlitchyBlock;
    }

    // Must process data first to get rid of false positives from ghost blocks
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        // If the offset is low, there probably isn't ghost blocks
        // However, if we would flag nofall, check for ghost blocks
        if (predictionComplete.getOffset() < 0.001 && (player.clientClaimsLastOnGround == player.onGround || player.inVehicle()))
            return;

        // This is meant for stuff like buggy blocks and mechanics on old clients
        // It was once for ghost blocks, although I've removed it for ghost blocks
        boolean shouldResync = isGhostBlock(player);

        if (shouldResync) {
            // I once used a buffer for this, but it should be very accurate now.
            if (player.clientClaimsLastOnGround != player.onGround) {
                // Rethink this.  Is there a better way to force the player's ground for the next tick?
                // No packet for it, so I think this is sadly the best way.
                player.onGround = player.clientClaimsLastOnGround;
            }

            predictionComplete.setOffset(0);
            player.getSetbackTeleportUtil().executeForceResync();
        }
    }
}
