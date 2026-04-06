package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.AbstractCheck;
import ac.reaper.reaperac.api.GrimUser;

public class FlagEvent extends GrimVerboseCheckEvent {

    public FlagEvent(GrimUser user, AbstractCheck check, String verbose) {
        super(user, check, verbose);
    }
}
