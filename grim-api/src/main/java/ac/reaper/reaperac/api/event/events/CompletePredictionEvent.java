package ac.reaper.reaperac.api.event.events;

import ac.reaper.reaperac.api.AbstractCheck;
import ac.reaper.reaperac.api.GrimUser;

public class CompletePredictionEvent extends GrimCheckEvent {
    private final double offset;

    public CompletePredictionEvent(GrimUser player, AbstractCheck check, double offset) {
        super(player, check);
        this.offset = offset;
    }

    public double getOffset() {
        return offset;
    }
}
