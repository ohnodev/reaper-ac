package ac.grim.reaperac.api.events;

import ac.grim.reaperac.api.AbstractCheck;
import ac.grim.reaperac.api.GrimUser;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Deprecated(since = "1.2.1.0", forRemoval = true)
public class CommandExecuteEvent extends FlagEvent {

    private static final HandlerList handlers = new HandlerList();
    @Getter private final String command;

    public CommandExecuteEvent(GrimUser player, AbstractCheck check, String verbose, String command) {
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
