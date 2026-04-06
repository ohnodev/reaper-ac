package ac.reaper.reaperac.api.events;

import ac.reaper.reaperac.api.AbstractCheck;
import ac.reaper.reaperac.api.ReaperUser;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Deprecated(since = "1.2.1.0", forRemoval = true)
public class FlagEvent extends Event implements GrimUserEvent, Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter private final ReaperUser user;
    @Getter private final AbstractCheck check;
    @Getter private final String verbose;
    private boolean cancelled;

    public FlagEvent(ReaperUser user, AbstractCheck check, String verbose) {
        super(true); // Async!
        this.user = user;
        this.check = check;
        this.verbose = verbose;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public double getViolations() {
        return check.getViolations();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isSetback() {
        return check.getViolations() > check.getSetbackVL();
    }


}
