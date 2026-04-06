package ac.reaper.reaperac.manager.init.stop;

import ac.reaper.reaperac.manager.init.Initable;

public interface StoppableInitable extends Initable {
    void stop();
}
