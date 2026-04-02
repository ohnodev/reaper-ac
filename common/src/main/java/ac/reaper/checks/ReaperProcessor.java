package ac.reaper.checks;

import ac.reaper.ReaperAPI;
import ac.reaper.api.AbstractProcessor;
import ac.reaper.api.config.ConfigReloadable;
import ac.reaper.utils.common.ConfigReloadObserver;

public abstract class ReaperProcessor implements AbstractProcessor, ConfigReloadable, ConfigReloadObserver {

    // Not everything has to be a check for it to process packets & be configurable

    @Override
    public void reload() {
        reload(ReaperAPI.INSTANCE.getConfigManager().getConfig());
    }

}
