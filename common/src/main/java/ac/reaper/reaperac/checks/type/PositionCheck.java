package ac.reaper.reaperac.checks.type;

import ac.grim.reaperac.api.AbstractCheck;
import ac.reaper.reaperac.utils.anticheat.update.PositionUpdate;

public interface PositionCheck extends AbstractCheck {

    default void onPositionUpdate(final PositionUpdate positionUpdate) {
    }
}
