package ac.reaper.reaperac.checks.type;

import ac.grim.reaperac.api.AbstractCheck;
import ac.reaper.reaperac.utils.anticheat.update.RotationUpdate;

public interface RotationCheck extends AbstractCheck {

    default void process(final RotationUpdate rotationUpdate) {
    }
}
