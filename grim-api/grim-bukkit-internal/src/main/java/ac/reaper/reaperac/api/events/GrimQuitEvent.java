package ac.reaper.reaperac.api.events;

import ac.reaper.reaperac.api.ReaperUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Deprecated(since = "1.2.1.0", forRemoval = true)
public class GrimQuitEvent extends Event implements GrimUserEvent {

    private static final HandlerList handlers = new HandlerList();
    private final ReaperUser user;

    public GrimQuitEvent(ReaperUser user) {
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
