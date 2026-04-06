package ac.reaper.reaperac.checks;

import ac.reaper.reaperac.GrimAPI;
import ac.grim.reaperac.api.AbstractProcessor;
import ac.grim.reaperac.api.config.ConfigReloadable;
import ac.reaper.reaperac.utils.common.ConfigReloadObserver;

public abstract class GrimProcessor implements AbstractProcessor, ConfigReloadable, ConfigReloadObserver {

    // Not everything has to be a check for it to process packets & be configurable

    @Override
    public void reload() {
        reload(GrimAPI.INSTANCE.getConfigManager().getConfig());
    }

}
