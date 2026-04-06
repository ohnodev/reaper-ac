package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.AbstractCheck;
import ac.reaper.reaperac.api.ReaperUser;
import ac.reaper.reaperac.api.event.Cancellable;
import ac.reaper.reaperac.api.event.GrimEvent;
import lombok.Getter;

public abstract class GrimCheckEvent extends GrimEvent implements GrimUserEvent, Cancellable {
    private final ReaperUser user;
    @Getter
    protected final AbstractCheck check;
    private boolean cancelled;

    public GrimCheckEvent(ReaperUser user, AbstractCheck check) {
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