package ac.reaper.utils.blockplace;

import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.BlockPlace;

public interface BlockPlaceFactory {
    void applyBlockPlaceToWorld(ReaperPlayer player, BlockPlace place);
}
