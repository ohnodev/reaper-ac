package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.AbstractCheck;
import ac.reaper.reaperac.api.ReaperUser;

public class FlagEvent extends GrimVerboseCheckEvent {

    public FlagEvent(ReaperUser user, AbstractCheck check, String verbose) {
        super(user, check, verbose);
    }
}
