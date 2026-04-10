package ac.reaper.reaperac.checks.impl.multiactions;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.BlockBreakCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;

@CheckData(name = "MultiActionsB", description = "Breaking blocks while using an item", experimental = true)
public class MultiActionsB extends Check implements BlockBreakCheck {
    public MultiActionsB(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (player.packetStateData.isSlowedByUsingItem() && (player.packetStateData.lastSlotSelected == player.packetStateData.getSlowedByUsingItemSlot() || player.packetStateData.itemInUseHand == InteractionHand.OFF_HAND)) {
            // this is vanilla on 1.7

            if (flagAndAlert() && shouldModifyPackets()) {
                blockBreak.cancel();
            }
        }
    }
}
