package ac.reaper.api.events;

import ac.reaper.api.ReaperUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Deprecated(since = "1.2.1.0", forRemoval = true)
public class ReaperQuitEvent extends Event implements ReaperUserEvent {

    private static final HandlerList handlers = new HandlerList();
    private final ReaperUser user;

    public ReaperQuitEvent(ReaperUser user) {
        super(true); // Async!
        this.user = user;
    }

    @Override
    public ReaperUser getUser() {
        return user;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
