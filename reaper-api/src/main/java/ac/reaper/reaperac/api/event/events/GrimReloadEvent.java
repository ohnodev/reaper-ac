package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.event.GrimEvent;

public class GrimReloadEvent extends GrimEvent {
    private final boolean success;

    public GrimReloadEvent(boolean success) {
        super(true); // Async
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
