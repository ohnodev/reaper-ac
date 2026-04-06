package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.ReaperUser;

public interface GrimUserEvent {
    ReaperUser getUser();
    default ReaperUser getPlayer() {
        return getUser();
    }
}

