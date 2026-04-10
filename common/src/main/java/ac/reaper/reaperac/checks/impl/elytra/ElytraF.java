package ac.reaper.reaperac.checks.impl.elytra;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PostPredictionCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(name = "ElytraF", description = "Started gliding while on ground")
public class ElytraF extends Check implements PostPredictionCheck {
    private boolean setback;

    public ElytraF(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION
                && new WrapperPlayClientEntityAction(event).getAction() == WrapperPlayClientEntityAction.Action.START_FLYING_WITH_ELYTRA
                && player.clientClaimsLastOnGround
                && flagAndAlert()
        ) {
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
