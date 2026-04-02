package ac.reaper.api.event.events;

import ac.reaper.api.AbstractCheck;
import ac.reaper.api.ReaperUser;

public abstract class ReaperVerboseCheckEvent extends ReaperCheckEvent {
    private final String verbose;

    public ReaperVerboseCheckEvent(ReaperUser user, AbstractCheck check, String verbose) {
        super(user, check);
        this.verbose = verbose;
    }

    public String getVerbose() {
        return verbose;
    }
}