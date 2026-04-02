package ac.reaper.checks.impl.breaking;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.BlockBreakCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;

@CheckData(name = "PositionBreakB")
public class PositionBreakB extends Check implements BlockBreakCheck {
    private final int releaseFace = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8) ? 0 : 255;
    private BlockFace lastFace;

    public PositionBreakB(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action == DiggingAction.START_DIGGING) {
            if (blockBreak.face == lastFace) {
                lastFace = null;
            }
        }

        if (lastFace != null) {
            flagAndAlert("lastFace=" + lastFace + ", action=" + blockBreak.action);
        }

        if (blockBreak.action == DiggingAction.CANCELLED_DIGGING) {
            lastFace = blockBreak.faceId == releaseFace ? null : blockBreak.face;
        }
    }
}
