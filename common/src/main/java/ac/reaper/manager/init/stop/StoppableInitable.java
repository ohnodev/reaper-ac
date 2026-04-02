package ac.reaper.manager.init.stop;

import ac.reaper.manager.init.Initable;

public interface StoppableInitable extends Initable {
    void stop();
}
