package ac.reaper.api.event.events;

import ac.reaper.api.AbstractCheck;
import ac.reaper.api.ReaperUser;

public class CommandExecuteEvent extends ReaperVerboseCheckEvent {
    private final String command;

    public CommandExecuteEvent(ReaperUser player, AbstractCheck check, String verbose, String command) {
        super(player, check, verbose);
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
