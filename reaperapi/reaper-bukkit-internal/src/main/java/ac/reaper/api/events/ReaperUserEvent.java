package ac.reaper.api.events;

import ac.reaper.api.ReaperUser;

@Deprecated(since = "1.2.1.0", forRemoval = true)
public interface ReaperUserEvent {

    ReaperUser getUser();

    default ReaperUser getPlayer() {
        return getUser();
    }

}
