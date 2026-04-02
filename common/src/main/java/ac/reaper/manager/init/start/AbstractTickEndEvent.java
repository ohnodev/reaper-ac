package ac.reaper.manager.init.start;

import ac.reaper.ReaperAPI;
import ac.reaper.player.ReaperPlayer;

// Intended for future events we inject all platforms at the end of a tick
public abstract class AbstractTickEndEvent implements StartableInitable {

    @Override
    public void start() {

    }

    protected void onEndOfTick(ReaperPlayer player) {
        player.checkManager.getPacketEntityReplication().onEndOfTickEvent();
    }

    protected boolean shouldInjectEndTick() {
        return ReaperAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("Reach.enable-post-packet", false);
    }
}
