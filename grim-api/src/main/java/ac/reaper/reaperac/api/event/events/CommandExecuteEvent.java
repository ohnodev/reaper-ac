package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.AbstractCheck;
import ac.reaper.reaperac.api.ReaperUser;

public class CommandExecuteEvent extends GrimVerboseCheckEvent {
    private final String command;

    public CommandExecuteEvent(ReaperUser player, AbstractCheck check, String verbose, String command) {
        super(player, check, verbose);
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
