package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.GrimUser;

public interface GrimUserEvent {
    GrimUser getUser();
    default GrimUser getPlayer() {
        return getUser();
    }
}

