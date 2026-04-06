package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.GrimUser;
import ac.reaper.reaperac.api.event.GrimEvent;

public class GrimQuitEvent extends GrimEvent implements GrimUserEvent {
    private final GrimUser user;

    public GrimQuitEvent(GrimUser user) {
        super(true); // Async
        this.user = user;
    }

    @Override
    public GrimUser getUser() {
        return user;
    }
}
