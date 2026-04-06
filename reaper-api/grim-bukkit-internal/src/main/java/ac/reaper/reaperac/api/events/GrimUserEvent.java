package ac.reaper.reaperac.api.events;

import ac.reaper.reaperac.api.ReaperUser;

@Deprecated(since = "1.2.1.0", forRemoval = true)
public interface GrimUserEvent {

    ReaperUser getUser();

    default ReaperUser getPlayer() {
        return getUser();
    }

}
