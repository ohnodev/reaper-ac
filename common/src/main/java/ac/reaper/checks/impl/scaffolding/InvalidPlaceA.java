package ac.reaper.checks.impl.scaffolding;

import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.BlockPlaceCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.BlockPlace;
import com.github.retrooper.packetevents.util.Vector3f;

@CheckData(name = "InvalidPlaceA", description = "Sent invalid cursor position")
public class InvalidPlaceA extends BlockPlaceCheck {
    public InvalidPlaceA(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        Vector3f cursor = place.cursor;
        if (cursor == null) return;
        if (!Float.isFinite(cursor.x) || !Float.isFinite(cursor.y) || !Float.isFinite(cursor.z)) {
            if (flagAndAlert() && shouldModifyPackets() && shouldCancel()) {
                place.resync();
            }
        }
    }
}
