package ac.reaper.manager.tick.impl;

import ac.reaper.ReaperAPI;
import ac.reaper.manager.tick.Tickable;
import ac.reaper.player.ReaperPlayer;

public class ClearRecentlyUpdatedBlocks implements Tickable {

    private static final int maxTickAge = 2;

    @Override
    public void tick() {
        for (ReaperPlayer player : ReaperAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            player.blockHistory.cleanup(ReaperAPI.INSTANCE.getTickManager().currentTick - maxTickAge);
        }
    }
}
