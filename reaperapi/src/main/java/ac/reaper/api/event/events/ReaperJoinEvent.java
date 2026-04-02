package ac.reaper.api.event.events;

import ac.reaper.api.ReaperUser;
import ac.reaper.api.event.ReaperEvent;

public class ReaperJoinEvent extends ReaperEvent implements ReaperUserEvent {
    private final ReaperUser user;

    public ReaperJoinEvent(ReaperUser user) {
        super(true); // Async
        this.user = user;
    }

    @Override
    public ReaperUser getUser() {
        return user;
    }
}
