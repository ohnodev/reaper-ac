package ac.reaper.checks.impl.multiactions;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.PacketCheck;
import ac.reaper.player.ReaperPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(name = "MultiActionsA", description = "Attacked while using an item", experimental = true)
public class MultiActionsA extends Check implements PacketCheck {
    public MultiActionsA(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.packetStateData.isSlowedByUsingItem() && (player.packetStateData.lastSlotSelected == player.packetStateData.getSlowedByUsingItemSlot() || player.packetStateData.itemInUseHand == InteractionHand.OFF_HAND)) {
            if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY && new WrapperPlayClientInteractEntity(event).getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK
                    || event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING && new WrapperPlayClientPlayerDigging(event).getAction() == DiggingAction.STAB) {
                if (flagAndAlert() && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
