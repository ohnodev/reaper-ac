package ac.reaper.reaperac.utils.blockplace;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.BlockPlace;

public interface BlockPlaceFactory {
    void applyBlockPlaceToWorld(GrimPlayer player, BlockPlace place);
}
