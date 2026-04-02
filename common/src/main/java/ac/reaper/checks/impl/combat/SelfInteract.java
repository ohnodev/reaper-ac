package ac.reaper.checks.impl.combat;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.PacketCheck;
import ac.reaper.player.ReaperPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "SelfInteract", description = "Interacted with self")
public class SelfInteract extends Check implements PacketCheck {
    public SelfInteract(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY
                && player.cameraEntity.isSelf() // TODO: should check for camera entity id?
                && new WrapperPlayClientInteractEntity(event).getEntityId() == player.entityID
                && flagAndAlert() && shouldModifyPackets() // Instant ban
        ) {
            event.setCancelled(true);
            player.onPacketCancel();
        }
    }
}
