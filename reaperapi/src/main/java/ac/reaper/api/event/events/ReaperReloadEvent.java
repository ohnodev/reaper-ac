package ac.reaper.api.event.events;

import ac.reaper.api.event.ReaperEvent;

public class ReaperReloadEvent extends ReaperEvent {
    private final boolean success;

    public ReaperReloadEvent(boolean success) {
        super(true); // Async
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
