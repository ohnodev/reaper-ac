package ac.grim.reaperac.api.events;

import ac.grim.reaperac.api.GrimUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Deprecated(since = "1.2.1.0", forRemoval = true)
public class GrimJoinEvent extends Event implements GrimUserEvent {

    private static final HandlerList handlers = new HandlerList();
    private final GrimUser user;

    public GrimJoinEvent(GrimUser user) {
        super(true); // Async!
        this.user = user;
    }

    @Override
    public GrimUser getUser() {
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
