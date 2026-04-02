package ac.reaper.predictionengine.blockeffects;

import ac.reaper.player.ReaperPlayer;

import java.util.List;

public interface BlockEffectsResolver {

    void applyEffectsFromBlocks(ReaperPlayer player, List<ReaperPlayer.Movement> movements);

}
