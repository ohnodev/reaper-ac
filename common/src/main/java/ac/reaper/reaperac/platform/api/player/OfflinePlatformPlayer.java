package ac.reaper.reaperac.platform.api.player;

import ac.grim.reaperac.api.GrimIdentity;

public interface OfflinePlatformPlayer extends GrimIdentity {

    boolean isOnline();

    String getName();
}
