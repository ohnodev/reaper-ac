package ac.reaper.checks.type;

import ac.reaper.api.AbstractCheck;
import ac.reaper.utils.anticheat.update.RotationUpdate;

public interface RotationCheck extends AbstractCheck {

    default void process(final RotationUpdate rotationUpdate) {
    }
}
