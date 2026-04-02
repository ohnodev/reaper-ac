package ac.reaper.platform.api.player;

import ac.reaper.api.ReaperIdentity;

public interface OfflinePlatformPlayer extends ReaperIdentity {

    boolean isOnline();

    String getName();
}
