package ac.reaper.api.events;

import ac.reaper.api.AbstractCheck;
import ac.reaper.api.ReaperUser;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Deprecated(since = "1.2.1.0", forRemoval = true)
public class CommandExecuteEvent extends FlagEvent {

    private static final HandlerList handlers = new HandlerList();
    @Getter private final String command;

    public CommandExecuteEvent(ReaperUser player, AbstractCheck check, String verbose, String command) {
        super(player, check, verbose); // Async!
        this.command = command;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
