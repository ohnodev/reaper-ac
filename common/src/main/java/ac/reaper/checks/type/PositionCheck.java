package ac.reaper.checks.type;

import ac.reaper.api.AbstractCheck;
import ac.reaper.utils.anticheat.update.PositionUpdate;

public interface PositionCheck extends AbstractCheck {

    default void onPositionUpdate(final PositionUpdate positionUpdate) {
    }
}
