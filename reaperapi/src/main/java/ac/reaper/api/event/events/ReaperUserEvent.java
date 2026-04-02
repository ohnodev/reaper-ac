package ac.reaper.api.event.events;

import ac.reaper.api.ReaperUser;

public interface ReaperUserEvent {
    ReaperUser getUser();
    default ReaperUser getPlayer() {
        return getUser();
    }
}

