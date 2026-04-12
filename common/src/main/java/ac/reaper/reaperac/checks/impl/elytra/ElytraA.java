package ac.reaper.reaperac.checks.impl.elytra;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PostPredictionCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "ElytraA", description = "Started gliding while already gliding")
public class ElytraA extends Check implements PostPredictionCheck {
    private boolean setback;

    public ElytraA(GrimPlayer player) {
        super(player);
    }

    public void onStartGliding(PacketReceiveEvent event) {
        if (player.isGliding && flagAndAlert()) {
            setback = true;
            if (shouldModifyPackets()) {
                event.setCancelled(true);
                player.onPacketCancel();
                player.resyncPose();
            }
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (setback) {
            setbackIfAboveSetbackVL();
            setback = false;
        }
    }
}
