package ac.grim.reaperac.api.event.events;

import ac.grim.reaperac.api.GrimUser;

public interface GrimUserEvent {
    GrimUser getUser();
    default GrimUser getPlayer() {
        return getUser();
    }
}

