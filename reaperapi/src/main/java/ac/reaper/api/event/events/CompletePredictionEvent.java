package ac.reaper.api.event.events;

import ac.reaper.api.AbstractCheck;
import ac.reaper.api.ReaperUser;

public class CompletePredictionEvent extends ReaperCheckEvent {
    private final double offset;

    public CompletePredictionEvent(ReaperUser player, AbstractCheck check, double offset) {
        super(player, check);
        this.offset = offset;
    }

    public double getOffset() {
        return offset;
    }
}
