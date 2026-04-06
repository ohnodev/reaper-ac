package ac.grim.reaperac.api.event.events;

import ac.grim.reaperac.api.AbstractCheck;
import ac.grim.reaperac.api.GrimUser;

public class FlagEvent extends GrimVerboseCheckEvent {

    public FlagEvent(GrimUser user, AbstractCheck check, String verbose) {
        super(user, check, verbose);
    }
}
