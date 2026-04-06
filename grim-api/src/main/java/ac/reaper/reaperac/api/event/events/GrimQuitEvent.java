package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.ReaperUser;
import ac.reaper.reaperac.api.event.GrimEvent;

public class GrimQuitEvent extends GrimEvent implements GrimUserEvent {
    private final ReaperUser user;

    public GrimQuitEvent(ReaperUser user) {
        super(true); // Async
        this.user = user;
    }

    @Override
    public ReaperUser getUser() {
        return user;
    }
}
