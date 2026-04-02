package ac.reaper.api.event.events;

import ac.reaper.api.AbstractCheck;
import ac.reaper.api.ReaperUser;
import ac.reaper.api.event.Cancellable;
import ac.reaper.api.event.ReaperEvent;
import lombok.Getter;

public abstract class ReaperCheckEvent extends ReaperEvent implements ReaperUserEvent, Cancellable {
    private final ReaperUser user;
    @Getter
    protected final AbstractCheck check;
    private boolean cancelled;

    public ReaperCheckEvent(ReaperUser user, AbstractCheck check) {
        super(true); // Async
        this.user = user;
        this.check = check;
    }

    @Override
    public ReaperUser getUser() {
        return user;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancellable() {
        return true;
    }

    public double getViolations() {
        return check.getViolations();
    }

    public boolean isSetback() {
        return check.getViolations() > check.getSetbackVL();
    }
}