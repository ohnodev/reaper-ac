package ac.reaper.reaperac.manager.init.start;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.player.GrimPlayer;

// Intended for future events we inject all platforms at the end of a tick
public abstract class AbstractTickEndEvent implements StartableInitable {

    @Override
    public void start() {

    }

    protected void onEndOfTick(GrimPlayer player) {
        player.checkManager.getPacketEntityReplication().onEndOfTickEvent();
    }

    protected boolean shouldInjectEndTick() {
        return GrimAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("Reach.enable-post-packet", false);
    }
}
