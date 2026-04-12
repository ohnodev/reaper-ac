package ac.reaper.reaperac.checks.impl.scaffolding;

import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.BlockPlaceCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.BlockPlace;

@CheckData(name = "InvalidPlaceB", description = "Sent impossible block face id")
public class InvalidPlaceB extends BlockPlaceCheck {
    public InvalidPlaceB(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (place.getFaceId() < 0 || place.getFaceId() > 5) {
            // ban
            if (flagAndAlert("direction=" + place.getFaceId()) && shouldModifyPackets() && shouldCancel()) {
                place.resync();
            }
        }
    }
}
