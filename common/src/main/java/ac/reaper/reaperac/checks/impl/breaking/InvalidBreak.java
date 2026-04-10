package ac.reaper.reaperac.checks.impl.breaking;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.BlockBreakCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.BlockBreak;

@CheckData(name = "InvalidBreak", description = "Sent impossible block face id")
public class InvalidBreak extends Check implements BlockBreakCheck {
    public InvalidBreak(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.faceId < 0 || blockBreak.faceId > 5) {
            // ban
            if (flagAndAlert("face=" + blockBreak.faceId + ", action=" + blockBreak.action) && shouldModifyPackets()) {
                blockBreak.cancel();
            }
        }
    }
}
