package ac.reaper.reaperac.predictionengine.blockeffects;

import ac.reaper.reaperac.player.GrimPlayer;

import java.util.List;

public interface BlockEffectsResolver {

    void applyEffectsFromBlocks(GrimPlayer player, List<GrimPlayer.Movement> movements);

}
