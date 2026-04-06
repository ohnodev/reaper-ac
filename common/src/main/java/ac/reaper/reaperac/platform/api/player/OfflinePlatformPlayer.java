package ac.reaper.reaperac.platform.api.player;

import ac.reaper.reaperac.api.ReaperIdentity;

public interface OfflinePlatformPlayer extends ReaperIdentity {

    boolean isOnline();

    String getName();
}
