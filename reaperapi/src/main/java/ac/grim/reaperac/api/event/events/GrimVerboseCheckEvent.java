package ac.grim.reaperac.api.event.events;

import ac.grim.reaperac.api.AbstractCheck;
import ac.grim.reaperac.api.GrimUser;

public abstract class GrimVerboseCheckEvent extends GrimCheckEvent {
    private final String verbose;

    public GrimVerboseCheckEvent(GrimUser user, AbstractCheck check, String verbose) {
        super(user, check);
        this.verbose = verbose;
    }

    public String getVerbose() {
        return verbose;
    }
}