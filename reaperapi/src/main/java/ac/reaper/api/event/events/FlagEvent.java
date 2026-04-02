package ac.reaper.api.event.events;

import ac.reaper.api.AbstractCheck;
import ac.reaper.api.ReaperUser;

public class FlagEvent extends ReaperVerboseCheckEvent {

    public FlagEvent(ReaperUser user, AbstractCheck check, String verbose) {
        super(user, check, verbose);
    }
}
