package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.ReaperUser;
import ac.reaper.reaperac.api.event.GrimEvent;

public class GrimJoinEvent extends GrimEvent implements GrimUserEvent {
    private final ReaperUser user;

    public GrimJoinEvent(ReaperUser user) {
        super(true); // Async
        this.user = user;
    }

    @Override
    public ReaperUser getUser() {
        return user;
    }
}
