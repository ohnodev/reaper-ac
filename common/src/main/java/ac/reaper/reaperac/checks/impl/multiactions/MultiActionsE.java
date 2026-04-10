package ac.reaper.reaperac.checks.impl.multiactions;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PacketCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(name = "MultiActionsE", description = "Swinging while using an item", experimental = true)
public class MultiActionsE extends Check implements PacketCheck {
    private boolean dropping;

    public MultiActionsE(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!dropping && player.packetStateData.isSlowedByUsingItem() && (player.packetStateData.lastSlotSelected == player.packetStateData.getSlowedByUsingItemSlot() || player.packetStateData.itemInUseHand == InteractionHand.OFF_HAND) && event.getPacketType() == PacketType.Play.Client.ANIMATION) {

            if (flagAndAlert() && shouldModifyPackets()) {
                event.setCancelled(true);
                player.onPacketCancel();
            }
        }

        if (!isAsync(event.getPacketType())) {
            dropping = false;
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            DiggingAction action = new WrapperPlayClientPlayerDigging(event).getAction();
            dropping = action == DiggingAction.DROP_ITEM || action == DiggingAction.DROP_ITEM_STACK;
        }
    }
}
